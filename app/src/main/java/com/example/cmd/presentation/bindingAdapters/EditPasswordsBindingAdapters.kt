package com.example.cmd.presentation.bindingAdapters

import androidx.databinding.BindingAdapter
import com.example.cmd.R
import com.example.cmd.presentation.states.PasswordsState
import com.example.cmd.setButtonColor
import com.google.android.material.button.MaterialButton



@BindingAdapter("setCorrectButtonState")
fun MaterialButton.setCorrectButtonState(state: PasswordsState) {
  isEnabled = when (state) {
    is PasswordsState.PasswordsCorrect -> {
      setButtonColor(R.color.amtheme)
      true
    }

    is PasswordsState.PasswordsIncorrect -> {
      setButtonColor(R.color.grey)
      false
    }
  }
}
