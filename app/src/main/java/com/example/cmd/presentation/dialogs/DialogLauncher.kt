package com.example.cmd.presentation.dialogs

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.example.cmd.presentation.actions.DialogActions
import com.example.cmd.presentation.dialogs.InfoDialog
import com.example.cmd.presentation.dialogs.InputDigitDialog
import com.example.cmd.presentation.dialogs.PasswordInputDialog
import com.example.cmd.presentation.dialogs.QuestionDialog
import com.example.cmd.presentation.dialogs.SelectItemDialog
import com.example.cmd.presentation.validators.ValidatorName

class DialogLauncher (
  private val fragmentManager: FragmentManager,
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
        showDigitInputDialog(title.asString(context), hint, message.asString(context), range)
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

  private fun showDigitInputDialog(title: String, hint: String, message: String, range: IntRange) {
    InputDigitDialog.show(fragmentManager,title,hint,message,range)
  }

  private fun showSelectItemDialog(title: String, message: String, selected: Int, items: ArrayList<String>) {
    SelectItemDialog.show(fragmentManager,title, message, selected, items)
  }

  private fun showPasswordInputDialog(title: String, hint: String, message: String, validatorName: ValidatorName, passwordComparison: String?=null) {
    PasswordInputDialog.show(fragmentManager,title, hint, message, validatorName, passwordComparison)
  }
}
