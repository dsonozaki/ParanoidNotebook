package com.example.cmd.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

//Запуск удаления файлов после загрузки
class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
      val myWorkRequest = OneTimeWorkRequest.Builder(AutoShredder::class.java).build()
      WorkManager.getInstance(context!!).enqueue(myWorkRequest)
    }
  }
}
