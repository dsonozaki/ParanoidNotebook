package com.example.cmd.presentation.actions

import com.example.cmd.presentation.utils.DateValidatorAllowed

sealed class LogsActions {
  class ShowUsualDialog(val value: DialogActions): LogsActions()
  class showDatePicker(val dateValidator: DateValidatorAllowed, val selection: Long): LogsActions()
}
