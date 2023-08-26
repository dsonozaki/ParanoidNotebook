package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.LogsDataSerializer
import com.example.cmd.domain.entities.LogsData
import com.example.cmd.domain.repositories.LogsDataRepository
import com.example.cmd.getEpochDays
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject


class LogsDataRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  coroutineScope: CoroutineScope,
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
      val logDates = it.logDates.mutate { list ->
        list.forEach { localDateTime ->
          if (checkOldDays(localDateTime, removePeriod, currentDay)) {
            list.remove(localDateTime)
            daysToClear.add(localDateTime)
          }
        }
      }
      it.copy(logDates = logDates, lastDayOfAutoDeletion = currentDay)
    }
    return daysToClear
  }

  private fun checkOldDays(localDateTime: String, removePeriod: Int, currentDay: Long): Boolean {
    return currentDay - LocalDateTime.parse(localDateTime).getEpochDays() > removePeriod
  }

  override suspend fun editLogsAutoRemoveTimeout(timeout: Int) {
    context.logsDataStore.updateData {
      it.copy(logsAutoRemovePeriod = timeout)
    }
  }

  override suspend fun addDayToLogs(day: String) {
    context.logsDataStore.updateData {
      val logDates = it.logDates.mutate { localDateTimes ->
        localDateTimes.add(day)
      }
      it.copy(logDates = logDates)
    }
  }

  override suspend fun removeDayFromLogs(day: String) {
    context.logsDataStore.updateData {
      val logDates = it.logDates.mutate { localDateTimes ->
        localDateTimes.remove(day)
      }
      it.copy(logDates = logDates)
    }
  }


  companion object {
    private const val DATASTORE_NAME = "logs_datastore.json"
  }

}
