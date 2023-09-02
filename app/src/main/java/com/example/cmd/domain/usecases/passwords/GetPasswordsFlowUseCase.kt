package com.example.cmd.domain.usecases.passwords

import com.example.cmd.domain.entities.Passwords
import com.example.cmd.domain.repositories.PasswordsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPasswordsFlowUseCase @Inject constructor(private val repository: PasswordsRepository) {
  operator fun invoke(): Flow<Passwords> {
    return repository.getPasswordsFlow()
  }
}
