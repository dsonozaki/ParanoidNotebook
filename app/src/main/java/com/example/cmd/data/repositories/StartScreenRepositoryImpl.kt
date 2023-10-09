package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.StartScreenDataSerializer
import com.example.cmd.domain.entities.AppInitStatus
import com.example.cmd.domain.entities.StartScreenData
import com.example.cmd.domain.repositories.StartScreenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class StartScreenRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context
) : StartScreenRepository {

  private val Context.startScreenDataStore by dataStore(DATASTORE_NAME, StartScreenDataSerializer)

  override suspend fun getStartScreenData(): StartScreenData =
    context.startScreenDataStore.data.first()


  override suspend fun saveText(text: String) {
      context.startScreenDataStore.updateData {
        it.copy(text = text)
    }
  }

  override suspend fun showHint() {
      context.startScreenDataStore.updateData {
        it.copy(appInitStatus = AppInitStatus.SHOW_HINT)
    }
  }

  override suspend fun finishInitialisation() {
      context.startScreenDataStore.updateData {
        it.copy(appInitStatus = AppInitStatus.INITIALISED)
      }
  }

  companion object {
    private const val DATASTORE_NAME = "start_screen_datastore.json"
  }
}
