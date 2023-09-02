package com.example.cmd.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.R
import com.example.cmd.domain.entities.LogState
import com.example.cmd.domain.entities.LogsData
import com.example.cmd.domain.usecases.logs.ChangeAutoDeletionTimeOutUseCase
import com.example.cmd.domain.usecases.logs.ClearLogsForDayUseCase
import com.example.cmd.domain.usecases.logs.GetLogsDataUseCase
import com.example.cmd.domain.usecases.logs.GetLogsUseCase
import com.example.cmd.domain.usecases.logs.LookLogsForDayUseCase
import com.example.cmd.formatDate
import com.example.cmd.presentation.states.LogsScreenState
import com.example.cmd.presentation.utils.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class LogsVM @Inject constructor(
  getLogsUseCase: GetLogsUseCase,
  getLogsDataUseCase: GetLogsDataUseCase,
  private val changeAutoDeletionTimeOutUseCase: ChangeAutoDeletionTimeOutUseCase,
  private val clearLogsForDayUseCase: ClearLogsForDayUseCase,
  private val lookLogsForDayUseCase: LookLogsForDayUseCase,
  private val updateStatesFlow: MutableSharedFlow<LogsScreenState>
) :
  ViewModel() {

  private var logsText = ""

  val logsState: StateFlow<LogsScreenState> =
    getLogsUseCase().map {
      Log.w("logsText",it.logs)
      if (it.logState == LogState.NEW_LOG_STRING) {
        logsText += it.logs.colorizeLogsString()
        return@map LogsScreenState.ViewLogs(it.today, UIText.ColoredHTMLText(logsText, R.color.amtheme, R.color.whitetext))
      }
      logsText = buildString {
        it.logs.lines().filter { it.isNotEmpty() }.forEach {
          append(it.colorizeLogsString())
          Log.w("line",it.colorizeLogsString())
        }
      }
      Log.w("colorizedText",logsText)
      LogsScreenState.ViewLogs(it.today, UIText.ColoredHTMLText(logsText, R.color.amtheme, R.color.whitetext))
    }.mergeWith(updateStatesFlow).stateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily, //?
      initialValue = LogsScreenState.Loading()
    )

  val logsData: StateFlow<LogsData> = getLogsDataUseCase().stateIn(scope = viewModelScope,
    started = SharingStarted.Lazily,
    initialValue = LogsData())

  private fun <T> Flow<T>.mergeWith(flow: Flow<T>): Flow<T> {
    return merge(this, flow)
  }

  private fun String.colorizeLogsString(): String {
    val groups =
      Regex("(\\d\\d:\\d\\d:\\d\\d) (.*)").find(
        this
      )?.groupValues ?: return "Wrong string, can't colorize"
    val time = groups[1]
    val message = groups[2]
    return "<span style=\"color: %s;\">$time</span> <span style=\"color: %s;\">$message</span><br>\n"
  }

  fun changeAutoDeletionTimeout(timeout: Int) {
    viewModelScope.launch {
      changeAutoDeletionTimeOutUseCase(timeout)
    }
  }


  fun clearLogsForDay() {
    viewModelScope.launch {
      clearLogsForDayUseCase(logsState.value.date.formatDate())
    }
  }

  private fun getDateTimeFromMillis(millis: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
      ZoneOffset.of(ZoneId.systemDefault().id))
  }

  fun openLogsForSelection(selection: Long) {
    viewModelScope.launch {
      updateStatesFlow.emit(LogsScreenState.Loading())
      lookLogsForDayUseCase(getDateTimeFromMillis(selection))
    }
  }

}
