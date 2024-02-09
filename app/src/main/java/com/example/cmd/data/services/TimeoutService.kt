package com.example.cmd.data.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.cmd.R
import com.example.cmd.domain.usecases.autodeletion.data.GetAutoDeletionDataUseCase
import com.example.cmd.domain.usecases.logs.WriteToLogsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@HiltWorker
class TimeoutService @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted workerParams: WorkerParameters,
  private val getAutoDeletionDataUseCase: GetAutoDeletionDataUseCase,
  private val writeToLogsUseCase: WriteToLogsUseCase):
  CoroutineWorker(context, workerParams) {
  override suspend fun doWork(): Result {
    val autoDeletionData = getAutoDeletionDataUseCase().first()
    if (!autoDeletionData.isActive) { //запущено ли автоудаление?
      return Result.success()
    }
    val timeOut = autoDeletionData.timeOut.toLong()
    writeToLogsUseCase(context.getString(R.string.deletion_confirmed, timeOut))
    delay(timeOut)
    AutoShredder.start(context)
    return Result.success()
  }

  companion object {
    const val WORK_NAME = "timeout_service"

    fun start(context: Context) {
      val workManager = WorkManager.getInstance(context)
      workManager.enqueueUniqueWork(WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<TimeoutService>().build()
      )
    }

  }

}
