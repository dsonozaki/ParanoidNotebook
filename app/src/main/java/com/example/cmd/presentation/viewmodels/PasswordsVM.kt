package com.example.cmd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.domain.repositories.PasswordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordsVM @Inject constructor(private val passwordsRepository: PasswordsRepository): ViewModel() {

  fun getPasswords() {
    passwordsRepository.passwords
  }

  fun updatePasswords(mainPassword: String, settingsPassword: String) {
    viewModelScope.launch {
      if (mainPassword.isNotEmpty()) {
        passwordsRepository.setMainPassword(mainPassword)
      }
      if (settingsPassword.isNotEmpty()) {
        passwordsRepository.setSettingsPassword(settingsPassword)
      }
    }
  }
}
