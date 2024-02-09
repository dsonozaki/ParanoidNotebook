package com.example.cmd.data.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CustomReceiver: BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    Log.w("received broadcast","ok")
    if (intent!!.action==ACTION) {
      AutoShredder.start(context!!)
    }
  }
  companion object {
    const val ACTION = "com.example.cmd.action.TRIGGER"
  }
}
