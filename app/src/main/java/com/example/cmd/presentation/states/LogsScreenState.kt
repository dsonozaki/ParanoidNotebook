package com.example.cmd.presentation.states

import com.example.cmd.presentation.utils.UIText
import java.time.LocalDateTime

sealed class LogsScreenState {

  class Loading(private val date: LocalDateTime = LocalDateTime.now()) :
    LogsScreenState()

  class ViewLogs(val date: LocalDateTime, val logs: UIText.ColoredHTMLText) :
    LogsScreenState()

}
