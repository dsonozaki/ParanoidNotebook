package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.Passwords
import kotlinx.coroutines.flow.Flow

interface PasswordsRepository {
  val passwords: Flow<Passwords>
  suspend fun setMainPassword(password: String)
  suspend fun setSettingsPassword(password: String)
}
