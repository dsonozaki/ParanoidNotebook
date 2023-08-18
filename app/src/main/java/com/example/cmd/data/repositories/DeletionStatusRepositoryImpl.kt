package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.DeletionStatusSerializer
import com.example.cmd.domain.entities.DeletionState
import com.example.cmd.domain.entities.DeletionStatus
import com.example.cmd.domain.repositories.DeletionStatusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeletionStatusRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
  DeletionStatusRepository {

  private val Context.deletionStatusDataStore by dataStore(DATASTORE_NAME, DeletionStatusSerializer)

  override val deletionStatus: Flow<DeletionStatus> = context.deletionStatusDataStore.data

  override suspend fun startDeletion() {
    withContext(IO) {
      context.deletionStatusDataStore.updateData {
        it.copy(deletionState = DeletionState.STARTED)
      }
    }
  }

  override suspend fun doNotStartDeletion() {
    withContext(IO) {
      context.deletionStatusDataStore.updateData {
        it.copy(deletionState = DeletionState.NOT_STARTED)
      }
    }
  }


  override suspend fun completeDeletion() {
    withContext(IO) {
      context.deletionStatusDataStore.updateData {
        it.copy(deletionState = DeletionState.NOT_STARTED)
      }
    }
  }

  override suspend fun preventDeletion() {
    withContext(IO) {
      context.deletionStatusDataStore.updateData {
        it.copy(deletionPreventionTimestamp = System.currentTimeMillis())
      }
    }
  }

  companion object {
    private const val DATASTORE_NAME = "deletion_status_datastore.json"
  }
}
