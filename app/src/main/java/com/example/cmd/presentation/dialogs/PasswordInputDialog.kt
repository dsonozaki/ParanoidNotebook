package com.example.cmd.presentation.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.example.cmd.R
import com.example.cmd.databinding.PasswordInputDialogFragmentBinding
import com.example.cmd.presentation.validators.BaseValidator
import com.example.cmd.presentation.validators.CreateStringsValidatorByName
import com.example.cmd.presentation.validators.ValidatorName
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PasswordInputDialog : DialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val message =
      arguments?.getString(MESSAGE) ?: throw RuntimeException("Message absent in PasswordInputDialog")
    val title =
      arguments?.getString(TITLE) ?: throw RuntimeException("Title absent in PasswordInputDialog")
    val hint =
      arguments?.getString(HINT) ?: throw RuntimeException("Hint absent in PasswordInputDialog")
    val requestKey = arguments?.getString(ARG_REQUEST_KEY)
      ?: throw RuntimeException("Request key absent in PasswordInputDialog")
    val validatorValue = arguments?.getString(VALIDATOR_TYPE)
      ?: throw RuntimeException("Validator absent in PasswordInputDialog")
    val validatorName = ValidatorName.valueOf(validatorValue)
    val passwordComparison = arguments?.getString(VALIDATOR_TYPE)
    val dialogBinding = PasswordInputDialogFragmentBinding.inflate(layoutInflater)
    dialogBinding.inputEditText.hint = hint
    val dialog = MaterialAlertDialogBuilder(requireActivity())
      .setTitle(title)
      .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
      .setPositiveButton(R.string.ok, null)
      .setNegativeButton(R.string.cancel) { dialog: DialogInterface, i: Int -> dialog.cancel() }
      .setView(dialogBinding.root)
      .create()

    dialog.setOnShowListener {
      dialogBinding.inputEditText.requestFocus()
      showKeyboard(dialogBinding.inputEditText)
      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
        val enteredText = dialogBinding.inputEditText.text.toString()
        val validator =if (passwordComparison!=null) {
          CreateStringsValidatorByName().create(validatorName, enteredText, passwordComparison)
        } else {
          CreateStringsValidatorByName().create(validatorName, enteredText)
        }
        val result = BaseValidator.validate(validator)
        if (!result.isSuccess) {
          dialogBinding.inputLayout.error = getString(result.message!!)
          return@setOnClickListener
        }
        parentFragmentManager.setFragmentResult(requestKey, bundleOf(RESPONSE to enteredText))
      }
      dismiss()
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
    private val TAG = QuestionDialog::class.simpleName
    const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
    const val RESPONSE = "RESPONSE"
    const val PASSWORD_COMPARISON = "PASSWORD_COMPARISON"
    private const val VALIDATOR_TYPE = "VALIDATOR_TYPE"
    fun show(
      fragmentManager: FragmentManager,
      title: String,
      hint: String,
      message: String,
      validatorName: ValidatorName,
      passwordComparison: String? = null
    ) {
      val fragment = InputDigitDialog().apply {
        arguments = bundleOf(
          TITLE to title,
          MESSAGE to message,
          HINT to hint,
          VALIDATOR_TYPE to validatorName.name,
          PASSWORD_COMPARISON to passwordComparison
        )
      }
      fragment.show(fragmentManager, TAG)
    }

    fun setupListener(
      fragmentManager: FragmentManager,
      lifecycleOwner: LifecycleOwner,
      listener: (String) -> Unit
    ) {
      fragmentManager.setFragmentResultListener(
        ARG_REQUEST_KEY, lifecycleOwner
      ) { _, result ->
        listener.invoke(
          result.getString(RESPONSE)
            ?: throw Exception("Response getting failed in PasswordInputDialog")
        )
      }
    }
  }
}
