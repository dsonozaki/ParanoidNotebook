package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.DeletionStatusSerializer
import com.example.cmd.domain.entities.DeletionStatus
import com.example.cmd.domain.repositories.DeletionStatusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeletionStatusRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
  DeletionStatusRepository {

  private val Context.deletionStatusDataStore by dataStore(DATASTORE_NAME, DeletionStatusSerializer)

  override val deletionStatus: Flow<DeletionStatus> = context.deletionStatusDataStore.data

  override suspend fun startDeletion() {
      context.deletionStatusDataStore.updateData {
        DeletionStatus.Deleting
    }
  }

  override suspend fun completeDeletion() {
      context.deletionStatusDataStore.updateData {
        DeletionStatus.Completed(System.currentTimeMillis())
      }
  }

  override suspend fun preventDeletion() {
      context.deletionStatusDataStore.updateData {
        DeletionStatus.Prevented(System.currentTimeMillis())
      }
  }

  companion object {
    private const val DATASTORE_NAME = "deletion_status_datastore.json"
  }
}
