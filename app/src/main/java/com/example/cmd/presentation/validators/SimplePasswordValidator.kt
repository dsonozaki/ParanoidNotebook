package com.example.cmd.presentation.validators

import com.example.cmd.R

class SimplePasswordValidator(private val password: String): BaseValidator() {
  override fun validate(): ValidateResult {
    if (password.length < 8) {
      return ValidateResult(false, R.string.short_password)
    }
    return ValidateResult(true)
  }
}
