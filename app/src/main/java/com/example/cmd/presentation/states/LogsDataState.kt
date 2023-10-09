package com.example.cmd.presentation.states

import com.example.cmd.presentation.utils.UIText
import java.time.LocalDateTime

sealed class LogsDataState(open val date: LocalDateTime) {

  class Loading(override val date: LocalDateTime = LocalDateTime.now()) :
    LogsDataState(date)

  class ViewLogs(override val date: LocalDateTime, val logs: UIText.ColoredHTMLText) :
    LogsDataState(date)

}
