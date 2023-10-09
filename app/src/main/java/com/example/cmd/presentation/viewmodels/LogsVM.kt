package com.example.cmd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.R
import com.example.cmd.domain.entities.LogState
import com.example.cmd.domain.usecases.logs.ChangeAutoDeletionTimeOutUseCase
import com.example.cmd.domain.usecases.logs.ClearLogsForDayUseCase
import com.example.cmd.domain.usecases.logs.GetLogsDataUseCase
import com.example.cmd.domain.usecases.logs.GetLogsUseCase
import com.example.cmd.domain.usecases.logs.LookLogsForDayUseCase
import com.example.cmd.formatDate
import com.example.cmd.getMillis
import com.example.cmd.presentation.actions.DialogActions
import com.example.cmd.presentation.actions.LogsActions
import com.example.cmd.presentation.states.LogsDataState
import com.example.cmd.presentation.utils.DateValidatorAllowed
import com.example.cmd.presentation.utils.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class LogsVM @Inject constructor(
  getLogsUseCase: GetLogsUseCase,
  private val getLogsDataUseCase: GetLogsDataUseCase,
  private val changeAutoDeletionTimeOutUseCase: ChangeAutoDeletionTimeOutUseCase,
  private val clearLogsForDayUseCase: ClearLogsForDayUseCase,
  private val lookLogsForDayUseCase: LookLogsForDayUseCase,
  private val updateStatesFlow: MutableSharedFlow<LogsDataState>,
  private val logsActionsChannel: Channel<LogsActions>
) :
  ViewModel() {

  val logsActionFlow = logsActionsChannel.receiveAsFlow()

  private var logsText = ""

  val logsState: StateFlow<LogsDataState> =
    getLogsUseCase().map {
      if (it.logState == LogState.NEW_LOG_STRING) {
        logsText += it.logs.colorizeLogsString()
        return@map LogsDataState.ViewLogs(
          it.today,
          UIText.ColoredHTMLText(logsText, R.color.amtheme, R.color.whitetext)
        )
      }
      logsText = buildString {
        it.logs.lines().filter { it.isNotEmpty() }.forEach {
          append(it.colorizeLogsString())
        }
      }
      LogsDataState.ViewLogs(
        it.today,
        UIText.ColoredHTMLText(logsText, R.color.amtheme, R.color.whitetext)
      )
    }.mergeWith(updateStatesFlow).stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = LogsDataState.Loading()
    )

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
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
  }

  fun openLogsForSelection(selection: Long) {
    viewModelScope.launch {
      updateStatesFlow.emit(LogsDataState.Loading())
      lookLogsForDayUseCase(getDateTimeFromMillis(selection))
    }
  }

  fun buildCalendar() {
    viewModelScope.launch {
      val logsData = getLogsDataUseCase().first()
      logsActionsChannel.send(
        LogsActions.showDatePicker(
          DateValidatorAllowed(logsData.logDates.toSet()),
          logsState.value.date.getMillis()
        )
      )
    }
  }

  fun showChangeTimeoutDialog() {
    viewModelScope.launch {
      val logsData = getLogsDataUseCase().first()
      logsActionsChannel.send(
        LogsActions.ShowUsualDialog(
          DialogActions.ShowInputDialog(
            UIText.StringResource(R.string.enter_timeout_logs),
            logsData.logsAutoRemovePeriod.toString(),
            UIText.StringResource(R.string.timeout_logs_long),
            1..10000
          )
        )
      )
    }
  }

  fun showClearLogsDialog() {
    viewModelScope.launch {
      val state = logsState.first()
      logsActionsChannel.send(
        LogsActions.ShowUsualDialog(
          DialogActions.ShowQuestionDialog(
            UIText.StringResource(R.string.clear_logs_question),
            UIText.StringResource(R.string.logs_clear_warning, state.date.formatDate()),
            CHANGE_TIMEOUT_REQUEST
          )
        )
      )
    }
  }

  fun showHelpDialog() {
    viewModelScope.launch {
      logsActionsChannel.send(
        LogsActions.ShowUsualDialog(
          DialogActions.ShowInfoDialog(
            UIText.StringResource(R.string.help),
            UIText.StringResource(R.string.logs_help)
          )
        )
      )
    }
  }

  companion object {
    const val CHANGE_TIMEOUT_REQUEST = "change_timeout_request"
  }
}
