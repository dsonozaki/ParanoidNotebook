package com.example.cmd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.domain.usecases.passwords.GetPasswordsUseCase
import com.example.cmd.domain.usecases.passwords.SetMainPasswordUseCase
import com.example.cmd.domain.usecases.passwords.SetSettingsPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordsVM @Inject constructor(
  private val getPasswordsUseCase: GetPasswordsUseCase,
  private val setMainPasswordUseCase: SetMainPasswordUseCase,
  private val setSettingsPasswordUseCase: SetSettingsPasswordUseCase
) : ViewModel() {

  suspend fun getPasswords() {
    getPasswordsUseCase()
  }

  fun updatePasswords(mainPassword: String, settingsPassword: String) {
    viewModelScope.launch {
      if (mainPassword.isNotEmpty()) {
        setMainPasswordUseCase(mainPassword)
      }
      if (settingsPassword.isNotEmpty()) {
        setSettingsPasswordUseCase(settingsPassword)
      }
    }
  }
}
