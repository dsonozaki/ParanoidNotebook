package com.example.cmd.presentation.states

import com.example.cmd.R
import com.example.cmd.presentation.UIText


sealed class StartScreenState {
  object Loading: StartScreenState()
  object Initialize: StartScreenState()
  object ShowHintEditing: StartScreenState()
  class ShowHint(hint: UIText.StringResource = UIText.StringResource(R.string.hint)) : StartScreenState()
  class NormalMode(val text: UIText.UsualString) : StartScreenState()
  class NormalModeEditing(val text: UIText.UsualString) : StartScreenState()
  class SecretMode(val text: UIText.UsualString) : StartScreenState()
  class SecretModeEditing(val text: UIText.UsualString) : StartScreenState()
}
