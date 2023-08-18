package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.AutoDeletionData
import kotlinx.coroutines.flow.SharedFlow

interface AutoDeletionDataRepository {
  val autoDeletionData : SharedFlow<AutoDeletionData>
  suspend fun putAutoDeletionTimeout(timeout: Int)
  suspend fun startAutoDeletion()
  suspend fun stopAutoDeletion()
}
