package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.AutoDeletionDataSerializer
import com.example.cmd.domain.entities.AutoDeletionData
import com.example.cmd.domain.repositories.AutoDeletionDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class AutoDeletionDataRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  coroutineScope: CoroutineScope
) :
  AutoDeletionDataRepository {

  private val Context.autoDeletionDataStore by dataStore(DATASTORE_NAME, AutoDeletionDataSerializer)

  override val autoDeletionData: SharedFlow<AutoDeletionData> =
    context.autoDeletionDataStore.data.shareIn(
      coroutineScope,
      SharingStarted.Lazily,
      replay = 1
    )

  override suspend fun putAutoDeletionTimeout(timeout: Int) {
    context.autoDeletionDataStore.updateData {
      it.copy(timeOut = timeout)
    }
  }

  override suspend fun startAutoDeletion() {
    context.autoDeletionDataStore.updateData {
      it.copy(isActive = true)
    }
  }

  override suspend fun stopAutoDeletion() {
    context.autoDeletionDataStore.updateData {
      it.copy(isActive = false)
    }
  }

  companion object {
    private const val DATASTORE_NAME = "auto_deletion_datastore.json"
  }
}
