package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.LogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface LogsTextRepository {
  val logEntity: Flow<LogEntity>
  val newFileNotification: Flow<String>
  suspend fun clearLogsForDays(days: Set<String>)
  suspend fun clearLogsForDay(day: String)
  suspend fun writeToLogs(string: String)
  suspend fun writeToLogsEncrypted(string: String)
  suspend fun readLogsForDay(localDateTime: LocalDateTime)
}
