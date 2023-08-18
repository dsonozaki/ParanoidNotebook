package com.example.cmd.presentation.viewmodels

import android.text.Spanned
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras.Empty.map
import com.example.cmd.domain.entities.LogEntity
import com.example.cmd.domain.entities.LogState
import com.example.cmd.domain.entities.LogsData
import com.example.cmd.domain.repositories.LogsDataRepository
import com.example.cmd.domain.repositories.LogsRepository
import com.example.cmd.model.PreferencesModel
import com.example.cmd.presentation.states.LogsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.internal.NopCollector.emit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LogsVM @Inject constructor(
  val logsRepository: LogsRepository,
  val logsDataRepository: LogsDataRepository,
  private val updateStatesFlow: MutableStateFlow<LogsScreenState>
) :
  ViewModel() {

  private fun <T> Flow<T>.mergeWith(another: Flow<T>): Flow<T> {
    return merge(this, another)
  }

  val logsState =
    logsRepository.logEntity.combineTransform(updateStatesFlow) { logEntity: LogEntity, logScreenState: LogsScreenState ->
      val newState: LogsScreenState = when (logEntity.logState) {
        LogState.NEW_LOG_FILE ->
          when (logScreenState) {
            is LogsScreenState.Loading -> LogsScreenState.ViewLogs(date = logEntity.today, logs = logEntity.logs)
            is StatesWithLogsDate
          }
      }
      emit(newState)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily,
      initialValue = LogsScreenState.Loading
    )

  private fun addStringToText(string: String) {

  }

  init {
    loadLogs()

  }

  fun getTimeLag() = model.getLong("logs_autoremove")

  fun loadLogs() {
    visibility.value = View.VISIBLE
    var result = ""
    //открытие сегодняшних логов
    File("${model.getFilesDir()}/Log/$day").bufferedReader().use {
      var time = ""
      var message = ""
      it.forEachLine {
        val groups =
          Regex("(\\d\\d:\\d\\d:\\d\\d) (.*)").find(
            it
          )?.groupValues
        if (groups != null) {
          time = groups[1]
          message = groups[2]
        } else
          message += it
        //дешифровка и окраска логов
        if (message.endsWith(" C")) {
          message = try {
            "<span style=\"color: #ff0000;\">" + model.decodeString(
              message.removeSuffix(" C")
            ) + "</span>"
          } catch (e: Exception) {
            "<span style=\"color: #ff0000;\">Не удалось расшифровать строку</span>"
          }
          result += "<span style=\"color: #ffcc00;\">$time</span> $message<br>"
        }
        if (message.endsWith(" E")) {
          message = message.removeSuffix(" E")
          result += "<span style=\"color: #ffcc00;\">$time</span> $message<br>"
        }
      }
    }
    text.value = HtmlCompat.fromHtml(result, HtmlCompat.FROM_HTML_MODE_LEGACY)
    visibility.value = View.GONE
    scroll.value = true
  }

  fun clearLogs() {
    File("${model.getFilesDir()}/Log/$day").bufferedWriter().use { it.write("") }
  }

  fun logTimeout(days: Long) {
    model.putLong("logs_autoremove", days)
  }

  //получение всех файлов с логами
  fun getAllowed() =
    File("${model.getFilesDir()}/Log").listFiles()?.map { it.name }?.toSet() ?: setOf()


}
