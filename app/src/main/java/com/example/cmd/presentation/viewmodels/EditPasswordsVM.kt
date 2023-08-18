package com.example.cmd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.cmd.domain.entities.Passwords
import com.example.cmd.presentation.states.PasswordsState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class EditPasswordsVM @AssistedInject constructor(
  @Assisted private val passwords: Passwords,
  private val _passwordsStateFlow: MutableStateFlow<PasswordsState>
) : ViewModel() {

  val passwordsStateFlow = _passwordsStateFlow.asStateFlow()

  init {
    if (!passwords.isEmpty()) {
      _passwordsStateFlow.value = PasswordsState.PasswordsCorrect()
    }
  }

  private fun String.dontContainEachOther(string: String): Boolean {
    return !this.contains(string) and !string.contains(
      this
    )
  }

  private fun checkPasswords(mainPassword: String, settingsPassword: String): Boolean {
    val currentMainPassword = mainPassword.ifEmpty {
      passwords.mainPass
    }
    val currentSettingsPassword = settingsPassword.ifEmpty {
      passwords.settingsPass
    }
    return currentMainPassword.dontContainEachOther(currentSettingsPassword) &&
      currentMainPassword.isNotEmpty() &&
      currentSettingsPassword.isNotEmpty()
  }

  fun passwordsChanged(mainPassword: String, settingsPassword: String) {
    _passwordsStateFlow.value = if (checkPasswords(mainPassword, settingsPassword)) {
      PasswordsState.PasswordsCorrect(mainPassword, settingsPassword)
    } else {
      PasswordsState.PasswordsIncorrect(mainPassword, settingsPassword)
    }
  }
}
