package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.Passwords
import kotlinx.coroutines.flow.Flow

interface PasswordsRepository {
  fun getPasswordsFlow(): Flow<Passwords>
  suspend fun setMainPassword(password: String)
  suspend fun setSettingsPassword(password: String)
}
