package com.example.cmd.domain.usecases.passwords

import com.example.cmd.domain.entities.Passwords
import com.example.cmd.domain.repositories.PasswordsRepository
import javax.inject.Inject

class GetPasswordsUseCase @Inject constructor(private val repository: PasswordsRepository) {
  suspend operator fun invoke(): Passwords {
    return repository.getPasswords()
  }
}
