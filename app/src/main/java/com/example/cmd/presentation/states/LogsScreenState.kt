package com.example.cmd.presentation.states

import com.google.android.material.datepicker.MaterialDatePicker

sealed interface LogsScreenState{

  object Loading: LogsScreenState
  data class ViewLogs(override val date: String, override val logs: String): LogsScreenState, StatesWithLogsDate(date, logs) {
    override fun copyDateLogs(date: String, logs: String): StatesWithLogsDate {
      return copy(date = date, logs = logs)
    }
  }

  data class ClearingLogs(override val date: String, override val logs: String): LogsScreenState, StatesWithLogsDate(date, logs) {
    override fun copyDateLogs(date: String, logs: String): StatesWithLogsDate {
      return copy(date = date, logs = logs)
    }
  }

  data class ChoosingLogs(override val date: String, override val logs: String, val datePicker: MaterialDatePicker<Long>): LogsScreenState, StatesWithLogsDate(date, logs) {
    override fun copyDateLogs(date: String, logs: String): StatesWithLogsDate {
      return copy(date = date, logs = logs)
    }
  }

  data class ChangeAutodeletionTimeOut(override val date: String, override val logs: String, val oldTimeout: Int, val newTimeOut: Int) : LogsScreenState, StatesWithLogsDate(date, logs) {
    override fun copyDateLogs(date: String, logs: String): StatesWithLogsDate {
      return copy(date = date, logs = logs)
    }
  }

  data class ShowHelp(override val date: String, override val logs: String) : LogsScreenState, StatesWithLogsDate(date, logs) {
    override fun copyDateLogs(date: String, logs: String): StatesWithLogsDate {
      return copy(date = date, logs = logs)
    }
  }
}

abstract class StatesWithLogsDate(open val date: String, open val logs: String) {
  abstract fun copyDateLogs(date: String, logs: String): StatesWithLogsDate
}

