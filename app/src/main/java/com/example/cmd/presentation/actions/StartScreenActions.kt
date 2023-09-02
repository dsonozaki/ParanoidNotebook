package com.example.cmd.presentation.actions

import com.example.cmd.presentation.utils.UIText

sealed class StartScreenActions {
  class ShowToast(val message: UIText.StringResource): StartScreenActions()
  data object WriteToLogs: StartScreenActions()
  data object CreatePasswords: StartScreenActions()
}
