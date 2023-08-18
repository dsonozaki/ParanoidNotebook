package com.example.cmd.presentation.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.cmd.R
import com.google.android.material.textfield.TextInputEditText

//Фрагмент диалога, требующего от пользователя ввода информации
class InputDialog : DialogFragment() {
  private lateinit var editBuilder: TextInputEditText

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val message = arguments?.getString("message")
    val title = arguments?.getString("title")
    val hint = arguments?.getString("hint")
    val dpi = arguments?.getFloat("dpi")
    editBuilder = TextInputEditText(requireActivity())
    with(editBuilder) {
      setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
      this.hint = hint
      setTextColor(
        resources.getColor(
          R.color.whitetext
        )
      )
      setHintTextColor(
        resources.getColor(
          R.color.lightgrey
        )
      )
      isSingleLine = true
    }
    savedInstanceState?.let { editBuilder.setText(it.getString("input")) }
    return AlertDialog.Builder(requireActivity(), R.style.DarkDialogTheme)
      .setTitle(title)
      .setMessage(Html.fromHtml(message))
      .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
      .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
      }
      .setView(editBuilder, (19 * dpi!!).toInt(), 0, (14 * dpi).toInt(), 0)
      .create()
  }

  override fun onResume() {
    super.onResume()
    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
      parentFragmentManager.setFragmentResult(
        "request",
        bundleOf("response" to arguments?.getString("type"), "extra" to editBuilder.text.toString())
      )

    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString("input", editBuilder.text.toString())
  }

}
