package com.example.cmd.presentation.states

sealed class PasswordsState {
  class PasswordsCorrect(val mainPassword: String = "", val settingsPassword: String = ""): PasswordsState()
  class PasswordsIncorrect(val mainPassword: String = "", val settingsPassword: String = ""): PasswordsState()
}
