package com.example.cmd.presentation.utils

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.example.cmd.presentation.actions.DialogActions
import com.example.cmd.presentation.dialogs.InfoDialog
import com.example.cmd.presentation.dialogs.InputDigitDialog
import com.example.cmd.presentation.dialogs.QuestionDialog

class DialogLauncher (
  val fragmentManager: FragmentManager,
  val context: Context?) {
  fun launchDialogFromAction(action: DialogActions) {
    when(action){
      is DialogActions.ShowInfoDialog -> with(action) {
        showInfoDialog(
          title.asString(context), message.asString(context)
        )
      }

      is DialogActions.ShowQuestionDialog -> with(action) {
        showQuestionDialog(title.asString(context), message.asString(context), requestKey)
      }
      is DialogActions.ShowInputDialog -> with(action) {
        showInputDialog(title.asString(context), hint, message.asString(context), range)
      }
    }
  }
  private fun showInfoDialog(title: String, message: String) {
    InfoDialog.show(
      fragmentManager,
      title, message
    )
  }

  private fun showQuestionDialog(title: String, message: String, requestKey: String) {
    QuestionDialog.show(fragmentManager, title, message, requestKey)
  }

  private fun showInputDialog(title: String, hint: String, message: String,range: IntRange) {
    InputDigitDialog.show(fragmentManager,title,hint,message,range)
  }
}
