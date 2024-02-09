package com.example.cmd.presentation.bindingAdapters

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.cmd.getColorForAttribute
import com.example.cmd.presentation.states.PasswordsState
import com.google.android.material.button.MaterialButton

fun MaterialButton.setButtonColor(color: Int) {
  setStrokeColorResource(color)
  setTextColor(ContextCompat.getColor(context, color))
}

@BindingAdapter("setCorrectButtonState")
fun MaterialButton.setCorrectButtonState(state: PasswordsState) {
  isEnabled = when (state) {
    is PasswordsState.PasswordsCorrect -> {
      Log.w("color",context.getColorForAttribute(com.google.android.material.R.attr.colorOutline).toString())
      setButtonColor(context.getColorForAttribute(com.google.android.material.R.attr.colorOutline))
      true
    }

    is PasswordsState.PasswordsIncorrect -> {
      setButtonColor(context.getColorForAttribute(com.google.android.material.R.attr.colorOutline))
      false
    }
  }
}
