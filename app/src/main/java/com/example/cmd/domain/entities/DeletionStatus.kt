package com.example.cmd.domain.entities

import android.os.SystemClock
import kotlinx.serialization.Serializable

@Serializable
sealed class DeletionStatus {
  @Serializable
  data class Prevented(val deletionPreventionTimestamp: Long = 0) : DeletionStatus() {
    fun isActualState() = deletionPreventionTimestamp> System.currentTimeMillis() - SystemClock.elapsedRealtime()
  }

  @Serializable
  data class Completed(val completionTimestamp: Long = 0): DeletionStatus() {
    fun isActualState() = completionTimestamp > System.currentTimeMillis() - SystemClock.elapsedRealtime()
  }

  @Serializable
  data object Deleting: DeletionStatus()
}

