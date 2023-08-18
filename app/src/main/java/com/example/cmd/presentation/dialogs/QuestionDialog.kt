package com.example.cmd.presentation.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.example.cmd.R

//Фрагмент диалога, предлагающего пользователю сделать определённый выбор
class QuestionDialog : DialogFragment() {
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val message = arguments?.getString("message")
    val title = arguments?.getString("title")
    return AlertDialog.Builder(requireActivity(), R.style.DarkDialogTheme)
      .setTitle(title)
      .setMessage(HtmlCompat.fromHtml(message!!, HtmlCompat.FROM_HTML_MODE_LEGACY))
      .setNegativeButton(R.string.cancel) { dialog: DialogInterface, i: Int -> dialog.cancel() }
      .setPositiveButton(R.string.ok) { _, which ->
        parentFragmentManager.setFragmentResult(
          "request",
          bundleOf("response" to arguments?.getString("type"))
        )
      }
      .create()
  }
}
