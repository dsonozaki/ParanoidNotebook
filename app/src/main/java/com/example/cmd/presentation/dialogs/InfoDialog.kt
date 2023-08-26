package com.example.cmd.presentation.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.cmd.R

//Фрагмент диалога, просто показывающего информацию пользователю
class InfoDialog :
  DialogFragment() {


  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args = requireArguments()
    val message = args.getString(MESSAGE) ?: throw RuntimeException("Message absent in InfoDialog")
    val title = args.getString(TITLE)?: throw RuntimeException("Title absent in InfoDialog")
    return AlertDialog.Builder(requireActivity(), R.style.DarkDialogTheme)
      .setTitle(title)
      .setMessage(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY))
      .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.cancel() }
      .create()
  }

  companion object {
    const val MESSAGE = "message"
    const val TITLE = "title"
    const val TAG = "infoDialog"
    fun show(fragmentManager: FragmentManager, title: String, message: String) {
      val fragment = InfoDialog().apply {
        arguments = bundleOf(QuestionDialog.MESSAGE to message, QuestionDialog.TITLE to title)
      }
      fragment.show(fragmentManager, TAG)
    }


  }
}
