package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.LogsDataSerializer
import com.example.cmd.domain.entities.LogsData
import com.example.cmd.domain.repositories.LogsDataRepository
import com.example.cmd.getEpochDays
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject


class LogsDataRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context
) :
  LogsDataRepository {

  private val Context.logsDataStore by dataStore(DATASTORE_NAME, LogsDataSerializer)

  override val logsData: Flow<LogsData> = context.logsDataStore.data

  override suspend fun clearOldLogsDays(): Set<String> {
    val currentDay = LocalDateTime.now().getEpochDays()
    val daysToClear: MutableSet<String> = mutableSetOf()
    context.logsDataStore.updateData {
      if (currentDay - it.lastDayOfAutoDeletion == 0L
      )
        return@updateData it
      val removePeriod = it.logsAutoRemovePeriod
      val logDates : MutableList<String> = mutableListOf()
      it.logDates.forEach { localDateTime ->
          if (checkOldDays(localDateTime, removePeriod, currentDay)) {
            daysToClear.add(localDateTime)
          } else {
            logDates.add(localDateTime)
          }
      }
      it.copy(logDates = logDates.toList(), lastDayOfAutoDeletion = currentDay)
    }
    return daysToClear
  }

  private fun checkOldDays(localDate: String, removePeriod: Int, currentDay: Long): Boolean {
    return currentDay - LocalDate.parse(localDate).toEpochDays() > removePeriod
  }

  override suspend fun editLogsAutoRemoveTimeout(timeout: Int) {
    context.logsDataStore.updateData {
      it.copy(logsAutoRemovePeriod = timeout)
    }
  }

  override suspend fun addDayToLogs(day: String) {
    context.logsDataStore.updateData {
      val logDates : MutableList<String> = it.logDates.toMutableList()
      logDates.add(day)
      it.copy(logDates = logDates.toList())
    }
  }

  override suspend fun changeLogsEnabled() {
    context.logsDataStore.updateData {
      it.copy(logsEnabled = !it.logsEnabled)
    }
  }

  override suspend fun removeDayFromLogs(day: String) {
    context.logsDataStore.updateData {
      val logDates : MutableList<String> = it.logDates.toMutableList()
      logDates.remove(day)
      it.copy(logDates = logDates.toList())
    }
  }



  companion object {
    private const val DATASTORE_NAME = "logs_datastore.json"
  }

}
