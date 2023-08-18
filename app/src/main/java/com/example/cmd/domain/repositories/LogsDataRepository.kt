package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.LogsData
import kotlinx.coroutines.flow.SharedFlow
import java.util.Calendar

interface LogsDataRepository {
  val logsData: SharedFlow<LogsData>
  suspend fun clearOldLogsDays(): Set<String>
  suspend fun editLogsAutoRemoveTimeout(timeout:Int)
  suspend fun addTodayToLogs()
  suspend fun removeDayFromLogs(day: Calendar)
}
