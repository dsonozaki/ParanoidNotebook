package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.LogsData
import kotlinx.coroutines.flow.Flow

interface LogsDataRepository {
  val logsData: Flow<LogsData>
  suspend fun clearOldLogsDays(): Set<String>
  suspend fun editLogsAutoRemoveTimeout(timeout:Int)
  suspend fun removeDayFromLogs(day: String)
  suspend fun addDayToLogs(day: String)
  suspend fun changeLogsEnabled()
}
