package com.example.cmd.presentation

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotebookApp: Application(), Configuration.Provider {

  @Inject
  lateinit var hiltWorkerFactory: HiltWorkerFactory
  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder()
      .setWorkerFactory(hiltWorkerFactory)
      .build()
  }

}
