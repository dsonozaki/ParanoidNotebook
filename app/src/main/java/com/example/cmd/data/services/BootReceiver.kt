package com.example.cmd.data.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

//Запуск удаления файлов после загрузки
class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (Intent.ACTION_AIRPLANE_MODE_CHANGED == intent!!.action) {
        val mode = intent.getBooleanExtra("state",true)
      val myWorkRequest = OneTimeWorkRequest.Builder(AutoShredder::class.java).build()
      WorkManager.getInstance(context!!).enqueue(myWorkRequest)
    }
  }

  companion object {
    const val ACTION_CLICK = "action_click"
  }

}
