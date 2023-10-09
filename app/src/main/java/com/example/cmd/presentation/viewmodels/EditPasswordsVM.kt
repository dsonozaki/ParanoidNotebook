package com.example.cmd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.domain.entities.Passwords
import com.example.cmd.domain.usecases.passwords.GetPasswordsFlowUseCase
import com.example.cmd.domain.usecases.passwords.SetMainPasswordUseCase
import com.example.cmd.domain.usecases.passwords.SetSettingsPasswordUseCase
import com.example.cmd.presentation.states.PasswordsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPasswordsVM @Inject constructor(
  private val passwordsUpdateFlow: MutableStateFlow<Passwords>,
  private val getPasswordsFlowUseCase: GetPasswordsFlowUseCase,
  private val setMainPasswordUseCase: SetMainPasswordUseCase,
  private val setSettingsPasswordUseCase: SetSettingsPasswordUseCase,
  private val goToMainScreenChannel: Channel<Unit>
) : ViewModel() {

  val goToMainScreenFlow = goToMainScreenChannel.receiveAsFlow()

  val passwordsStateFlow = getPasswordsFlowUseCase().combineTransform(passwordsUpdateFlow) {
    passwordsSaved: Passwords, passwordsEntered : Passwords ->
    if (checkPasswords(passwordsEntered, passwordsSaved)) {
      emit(PasswordsState.PasswordsCorrect)
    } else {
      emit(PasswordsState.PasswordsIncorrect)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PasswordsState.PasswordsIncorrect)

  fun updatePasswords(mainPassword: String,settingsPassword: String) {
    viewModelScope.launch {
      if (mainPassword.isNotEmpty()) {
        setMainPasswordUseCase(mainPassword)
      }
      if (settingsPassword.isNotEmpty()) {
        setSettingsPasswordUseCase(settingsPassword)
      }
      goToMainScreenChannel.send(Unit)
    }
  }

  private fun String.dontContainEachOther(string: String): Boolean {
    return !this.contains(string) and !string.contains(
      this
    )
  }

  private fun checkPasswords(enteredPasswords: Passwords, savedPasswords: Passwords): Boolean {
    val currentMainPassword = enteredPasswords.mainPass.ifEmpty {
      savedPasswords.mainPass
    }
    val currentSettingsPassword = enteredPasswords.settingsPass.ifEmpty {
      savedPasswords.settingsPass
    }
    return currentMainPassword.dontContainEachOther(currentSettingsPassword) &&
      currentMainPassword.isNotEmpty() &&
      currentSettingsPassword.isNotEmpty()
  }

  fun passwordsChanged(mainPassword: String, settingsPassword: String) {
    viewModelScope.launch {
      passwordsUpdateFlow.emit(Passwords(settingsPassword,mainPassword))
    }
  }
}
