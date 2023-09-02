package com.example.cmd.presentation.states


sealed class StartScreenState {
  data object Loading: StartScreenState()
  data object ShowHintEditing : StartScreenState()
  data object ShowHint : StartScreenState()
  class NormalMode(val text: String?=null) : StartScreenState()
  data object NormalModeEditing : StartScreenState()
  data object SecretMode : StartScreenState()
  data object SecretModeEditing : StartScreenState()
}
