package com.example.cmd.presentation.bindingAdapters

import android.view.View
import android.widget.ProgressBar
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.example.cmd.R
import com.example.cmd.presentation.states.StartScreenState
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("progressBarFromStartState")
fun ProgressBar.statusFromStartState(state: StartScreenState) {
  visibility = if (state is StartScreenState.Loading) View.VISIBLE
  else View.GONE
}

@BindingAdapter("textInputFromStartState")
fun TextInputEditText.statusFromStartState(state: StartScreenState) {
  isEnabled = when (state) {
    is StartScreenState.Loading, StartScreenState.SecretMode -> {
      false
    }
    is StartScreenState.ShowHint -> {
      hint =
        HtmlCompat.fromHtml(context.getString(R.string.hint), HtmlCompat.FROM_HTML_MODE_LEGACY)
      false
    }
    is StartScreenState.SecretModeEditing -> {
      hint = context.getString(R.string.entertext)
      true
    }
    is StartScreenState.NormalMode -> {
      hint = context.getString(R.string.entertext)
      state.text?.let { setText(it) }
      false
    }
    else -> true
  }
}
@BindingAdapter("textInputLayoutFromStartState")
fun TextInputLayout.statusFromStartState(state: StartScreenState) {
  visibility = when (state) {
    is StartScreenState.Loading -> View.GONE
    else -> View.VISIBLE
  }
}
