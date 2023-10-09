package com.example.cmd.presentation.actions

import com.example.cmd.presentation.utils.UIText

sealed class DeletionSettingsAction {
  class ShowUsualDialog(val value: DialogActions): DeletionSettingsAction()
  class ShowPriorityEditor(val title: UIText.StringResource, val hint: String, val message: UIText.StringResource, val uri: String, val range: IntRange): DeletionSettingsAction()
}
