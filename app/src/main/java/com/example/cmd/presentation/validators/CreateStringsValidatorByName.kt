package com.example.cmd.presentation.validators

class CreateStringsValidatorByName {
  fun create(validatorName: ValidatorName, vararg args: String): BaseValidator {
    return when (validatorName) {
        ValidatorName.SIMPLE_PASSWORD_VALIDATOR -> if (args.size == 1) {
          SimplePasswordValidator(args[0])
        } else {
          throw RuntimeException("Wrong arguments for SIMPLE_PASSWORD_VALIDATOR")
        }
      ValidatorName.PASSWORD_VALIDATOR -> if (args.size == 2) {
        PasswordValidator(args[0],args[1])
      } else {
        throw RuntimeException("Wrong arguments for PASSWORD_VALIDATOR")
      }
    }
  }


}
