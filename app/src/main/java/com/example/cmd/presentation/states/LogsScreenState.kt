package com.example.cmd.presentation.states

import com.example.cmd.presentation.utils.UIText
import java.time.LocalDateTime

sealed class LogsScreenState(open val date: LocalDateTime) {

  class Loading(override val date: LocalDateTime = LocalDateTime.now()) :
    LogsScreenState(date)

  class ViewLogs(override val date: LocalDateTime, val logs: UIText.ColoredHTMLText) :
    LogsScreenState(date)

}
