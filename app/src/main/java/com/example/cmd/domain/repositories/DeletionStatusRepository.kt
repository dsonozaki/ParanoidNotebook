package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.DeletionStatus
import kotlinx.coroutines.flow.Flow

interface DeletionStatusRepository {
  val deletionStatus: Flow<DeletionStatus>

  suspend fun startDeletion()

  suspend fun doNotStartDeletion()

  suspend fun completeDeletion()

  suspend fun preventDeletion()
}
