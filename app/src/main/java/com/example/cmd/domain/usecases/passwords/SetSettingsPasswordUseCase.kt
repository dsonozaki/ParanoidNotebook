package com.example.cmd.domain.usecases.passwords

import com.example.cmd.domain.repositories.PasswordsRepository
import javax.inject.Inject

class SetSettingsPasswordUseCase @Inject constructor(private val repository: PasswordsRepository) {
  suspend operator fun invoke(password: String) {
    repository.setSettingsPassword(password)
  }
}
