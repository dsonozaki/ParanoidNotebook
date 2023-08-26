package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.LogEntity
import kotlinx.coroutines.flow.Flow

interface LogsTextRepository {
  val logEntity: Flow<LogEntity>
  val newFileNotification: Flow<String>
  suspend fun clearLogsForDays(days: Set<String>)
  suspend fun clearLogsForDay(day: String)
  suspend fun readLogsForDay(day:String)
  suspend fun writeToLogs(string: String)
  suspend fun writeToLogsEncrypted(string: String)
}
