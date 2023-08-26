package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.Passwords

interface PasswordsRepository {
  suspend fun getPasswords(): Passwords
  suspend fun setMainPassword(password: String)
  suspend fun setSettingsPassword(password: String)
}
