package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.AutoDeletionDataSerializer
import com.example.cmd.domain.entities.AutoDeletionData
import com.example.cmd.domain.repositories.AutoDeletionDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AutoDeletionDataRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
) :
  AutoDeletionDataRepository {

  private val Context.autoDeletionDataStore by dataStore(DATASTORE_NAME, AutoDeletionDataSerializer)


  override val autoDeletionData: Flow<AutoDeletionData> =
    context.autoDeletionDataStore.data

  override suspend fun putAutoDeletionTimeout(timeout: Int) {
    context.autoDeletionDataStore.updateData {
      it.copy(timeOut = timeout)
    }
  }

  override suspend fun xiomiNotificationSent() {
    context.autoDeletionDataStore.updateData {
      it.copy(xiaomiPhoneNotificationRequired = false)
    }
  }

  override suspend fun switchAutoDeletionStatus() {
    context.autoDeletionDataStore.updateData {
      it.copy(isActive = !it.isActive)
    }
  }

  companion object {
    private const val DATASTORE_NAME = "auto_deletion_datastore.json"
  }
}
