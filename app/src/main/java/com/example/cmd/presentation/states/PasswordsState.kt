package com.example.cmd.presentation.states

sealed class PasswordsState {
  data object PasswordsCorrect : PasswordsState()
  data object PasswordsIncorrect : PasswordsState()
}
