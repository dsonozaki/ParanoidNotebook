package com.example.cmd.data.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager


//Запуск удаления файлов после загрузки
class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
      val workManager = WorkManager.getInstance(context!!)
      workManager.enqueueUniqueWork(AutoShredder.WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        AutoShredder.getInstance()
      )
    }
  }

}
