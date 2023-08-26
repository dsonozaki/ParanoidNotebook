package com.example.cmd.presentation.states

import com.example.cmd.presentation.utils.UIText


sealed class StartScreenState {
  object Loading: StartScreenState()
  object Initialize: StartScreenState()
  object ShowHintEditing: StartScreenState()
  object ShowHint : StartScreenState()
  class NormalMode(val text: UIText.UsualString) : StartScreenState()
  class NormalModeEditing(val text: UIText.UsualString) : StartScreenState()
  class SecretMode(val text: UIText.UsualString) : StartScreenState()
  class SecretModeEditing(val text: UIText.UsualString) : StartScreenState()
}
