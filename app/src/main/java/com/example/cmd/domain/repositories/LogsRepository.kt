package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.LogEntity
import com.example.cmd.domain.entities.LogsData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface LogsRepository {
  val logsData: Flow<LogsData>

  val logsText: Flow<LogEntity>
  suspend fun clearLogsForDay(day: String)
  suspend fun changeAutoDeletionTimeOut(timeout: Int)
  suspend fun writeToLogs(string: String)
  suspend fun writeToLogsEncrypted(string: String)

  suspend fun lookLogsForDay(localDateTime: LocalDateTime)
}
