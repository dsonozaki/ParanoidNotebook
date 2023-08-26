package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.AutoDeletionData
import kotlinx.coroutines.flow.Flow

interface AutoDeletionDataRepository {
  val autoDeletionData : Flow<AutoDeletionData>
  suspend fun putAutoDeletionTimeout(timeout: Int)
  suspend fun switchAutoDeletionStatus()
  suspend fun xiomiNotificationSent()
}
