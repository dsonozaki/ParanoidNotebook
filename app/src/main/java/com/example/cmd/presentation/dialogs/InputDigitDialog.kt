package com.example.cmd.presentation.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.example.cmd.R
import com.example.cmd.databinding.InputDialogFragmentBinding

//Фрагмент диалога, требующего от пользователя ввода информации
class InputDigitDialog : DialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val message = arguments?.getString(MESSAGE) ?: throw RuntimeException("Message absent in InputDigitDialog")
    val title = arguments?.getString(TITLE) ?: throw RuntimeException("Title absent in InputDigitDialog")
    val hint = arguments?.getString(HINT) ?: throw RuntimeException("Hint absent in InputDigitDialog")
    val dialogBinding = InputDialogFragmentBinding.inflate(layoutInflater)
    dialogBinding.inputEditText.hint = hint
    val dialog =  AlertDialog.Builder(requireActivity(), R.style.DarkDialogTheme)
      .setTitle(title)
      .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
      .setPositiveButton(R.string.ok, null)
      .setView(dialogBinding.root)
      .create()

    dialog.setOnShowListener {
      dialogBinding.inputEditText.requestFocus()
      showKeyboard(dialogBinding.inputEditText)

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
        val enteredText = dialogBinding.inputEditText.text.toString()
        if (enteredText.isBlank()) {
          dialogBinding.inputEditText.error = getString(R.string.empty_value_in_dialog)
          return@setOnClickListener
        }
        val text = enteredText.toIntOrNull()
        if (text == null){
          dialogBinding.inputEditText.error = getString(R.string.incorrect_number_in_dialog)
          return@setOnClickListener
        }
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESPONSE to text))
        dismiss()
      }
    }
    dialog.setOnDismissListener { hideKeyboard(dialogBinding.inputEditText) }

    return dialog
  }



  private fun showKeyboard(view: View) {
    view.post {
      getInputMethodManager(view).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
  }

  private fun hideKeyboard(view: View) {
    getInputMethodManager(view).hideSoftInputFromWindow(view.windowToken, 0)
  }

  private fun getInputMethodManager(view: View): InputMethodManager {
    val context = view.context
    return context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  }

  companion object {
    const val MESSAGE = "message"
    const val TITLE = "title"
    const val HINT = "hint"
    val TAG = QuestionDialog::class.simpleName
    val REQUEST_KEY = "$TAG ENTERED"
    const val RESPONSE = "RESPONSE"
    fun show(fragmentManager: FragmentManager, title: String, hint: String, message: String) {
      val fragment = InputDigitDialog().apply {
        arguments = bundleOf(TITLE to title,MESSAGE to message, HINT to hint)
      }
      fragment.show(fragmentManager,TAG)
    }

    fun setupListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, listener: (Int) -> Unit) {
      fragmentManager.setFragmentResultListener(REQUEST_KEY,lifecycleOwner
      ) { _, result -> listener.invoke(result.getInt(RESPONSE)) }
    }

  }
}
