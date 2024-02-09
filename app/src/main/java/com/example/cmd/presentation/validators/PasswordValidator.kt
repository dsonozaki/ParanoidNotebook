package com.example.cmd.presentation.validators

import com.example.cmd.R

class PasswordValidator(private val password: String, private val comparisonPassword: String): BaseValidator() {
  override fun validate(): ValidateResult {
    val firstCheckResult = SimplePasswordValidator(password).validate()
    if (!firstCheckResult.isSuccess) {
      return firstCheckResult
    }
    if (password.contains(comparisonPassword) || comparisonPassword.contains(password)) {
      return ValidateResult(false, R.string.passwords_intersection)
    }
    return ValidateResult(true)
  }
}
