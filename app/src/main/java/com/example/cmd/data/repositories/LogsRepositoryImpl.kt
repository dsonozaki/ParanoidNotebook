package com.example.cmd.data.repositories

import android.content.Context
import com.example.cmd.domain.repositories.LogsDataRepository
import com.example.cmd.domain.repositories.LogsRepository
import com.example.cmd.domain.repositories.LogsTextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogsRepositoryImpl @Inject constructor(
  private val logsTextRepository: LogsTextRepository,
  private val logsDataRepository: LogsDataRepository,
  private val coroutineScope: CoroutineScope,
  private val context: Context
) : LogsRepository {

  override val logsText = logsTextRepository.logEntity

  override val logsData = logsDataRepository.logsData

  init {
    coroutineScope.launch {
      val daysToClear = logsDataRepository.clearOldLogsDays()
      logsTextRepository.clearLogsForDays(daysToClear)
      logsTextRepository.newFileNotification.collect {
        logsDataRepository.addDayToLogs(it)
      }
    }
  }

  override suspend fun clearLogsForDay(day: String) {
    logsDataRepository.removeDayFromLogs(day)
    logsTextRepository.clearLogsForDay(day)
  }

  override suspend fun changeAutoDeletionTimeOut(timeout: Int) {
    logsDataRepository.editLogsAutoRemoveTimeout(timeout)
  }

  override suspend fun lookLogsForDay(day: String) {
    logsTextRepository.readLogsForDay(day)
  }

  override suspend fun writeToLogs(string: String) {
    logsTextRepository.writeToLogs(string)
  }

  override suspend fun writeToLogsEncrypted(string: String) {
    logsTextRepository.writeToLogsEncrypted(string)
  }
}
