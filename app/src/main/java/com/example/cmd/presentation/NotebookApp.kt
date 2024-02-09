package com.example.cmd.presentation

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@HiltAndroidApp
class NotebookApp: Application(), Configuration.Provider {

  @EntryPoint
  @InstallIn(SingletonComponent::class)
  interface HiltWorkerFactoryEntryPoint {
    fun workerFactory(): HiltWorkerFactory
  }

  @Inject
  lateinit var hiltWorkerFactory: HiltWorkerFactory

  override val workManagerConfiguration =
    Configuration.Builder()
      .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
      .build()

}
