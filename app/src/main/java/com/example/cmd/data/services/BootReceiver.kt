package com.example.cmd.data.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


//Запуск удаления файлов после загрузки
class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
      TimeoutService.start(context!!)
    }
  }

}
