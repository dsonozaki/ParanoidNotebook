package com.example.cmd.presentation.validators

import com.example.cmd.R

class NoteValidator() {
  fun validate(title: String, text: String): ValidateResult {
    if (title.isEmpty()) {
      return ValidateResult(false, R.string.empty_title)
    }
    if (text.isEmpty()) {
      return ValidateResult(false,R.string.empty_text)
    }
    return ValidateResult(true)
  }
}
