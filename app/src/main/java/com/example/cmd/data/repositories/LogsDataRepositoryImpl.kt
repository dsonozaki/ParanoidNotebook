package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.LogsDataSerializer
import com.example.cmd.domain.entities.LogsData
import com.example.cmd.domain.repositories.LogsDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

fun Calendar.formatDate(): String {
  val myFormat = "dd/MM/yy"
  val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
  return sdf.format(this.time)
}

class LogsDataRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  coroutineScope: CoroutineScope
) :
  LogsDataRepository {

  private val Context.logsDataStore by dataStore(DATASTORE_NAME, LogsDataSerializer)

  override val logsData: SharedFlow<LogsData> = context.logsDataStore.data.shareIn(
    coroutineScope,
    SharingStarted.Lazily,
    replay = 1
  )

  override suspend fun clearOldLogsDays(): Set<String> {
    val currentDay = java.time.LocalDate.now().toEpochDay()
    val daysToClear: MutableSet<String> = mutableSetOf()
    context.logsDataStore.updateData {
      if (currentDay - it.lastDayOfAutoDeletion == 0L
      )
        return@updateData it
      val removePeriod = it.logsAutoRemovePeriod
      val logDates = it.logDates.mutate { list ->
        list.forEach { calendar ->
          if (checkOldDays(calendar, removePeriod, currentDay)) {
            list.remove(calendar)
            daysToClear.add(calendar.formatDate())
          }
        }
      }
      it.copy(logDates = logDates, lastDayOfAutoDeletion = currentDay)
    }
    return daysToClear
  }

  private fun checkOldDays(calendar: Calendar, removePeriod: Int, currentDay: Long): Boolean {
    return calendar.getEpochDay() - currentDay > removePeriod
  }

  private fun Calendar.getEpochDay(): Long {
    return java.time.LocalDate.ofYearDay(
      this.get(Calendar.YEAR),
      this.get(Calendar.DAY_OF_YEAR)
    ).toEpochDay()
  }

  override suspend fun editLogsAutoRemoveTimeout(timeout: Int) {
    context.logsDataStore.updateData {
      it.copy(logsAutoRemovePeriod = timeout)
    }
  }

  override suspend fun addTodayToLogs() {
    context.logsDataStore.updateData {
      val logDates = it.logDates.mutate { calendar ->
        calendar.add(Calendar.getInstance())
      }
      it.copy(logDates = logDates)
    }
  }

  override suspend fun removeDayFromLogs(day: Calendar) {
    context.logsDataStore.updateData {
      val logDates = it.logDates.mutate { calendar ->
        calendar.remove(day)
      }
      it.copy(logDates = logDates)
    }
  }


  companion object {
    private const val DATASTORE_NAME = "logs_datastore.json"
  }

}
