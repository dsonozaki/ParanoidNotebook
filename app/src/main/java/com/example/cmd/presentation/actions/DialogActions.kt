package com.example.cmd.presentation.actions

import com.example.cmd.presentation.utils.UIText

sealed class DialogActions {
  class ShowQuestionDialog(
    val title: UIText.StringResource,
    val message: UIText.StringResource,
    val requestKey: String
  ) : DialogActions()

  class ShowInfoDialog(val title: UIText.StringResource, val message: UIText.StringResource) :
    DialogActions()

  class ShowInputDialog(
    val title: UIText.StringResource,
    val hint: String,
    val message: UIText.StringResource,
    val range: IntRange
  ) :
    DialogActions()
}
