package com.example.cmd.presentation.validators

class SettingsValidator(private val password: String, private val comparisonPassword: String, private val timeOut: String, private val autoDeletionActive: Boolean): BaseValidator() {
  override fun validate(): ValidateResult {
    if (!autoDeletionActive) {
      return ValidateResult(true)
    }
    val passwordValidation = PasswordValidator(password,comparisonPassword).validate()
    if (!passwordValidation.isSuccess) {
      return passwordValidation
    }
    val digitValidation = DigitInBoundsValidator(timeOut,1,1000).validate()
    if (!digitValidation.isSuccess) {
      return digitValidation
    }
    else return ValidateResult(true)
  }

}
