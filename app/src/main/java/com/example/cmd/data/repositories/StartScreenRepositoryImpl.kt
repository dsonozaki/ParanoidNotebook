package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.StartScreenDataSerializer
import com.example.cmd.domain.entities.AppInitStatus
import com.example.cmd.domain.entities.StartScreenData
import com.example.cmd.domain.repositories.StartScreenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StartScreenRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context
) : StartScreenRepository {

  private val Context.startScreenDataStore by dataStore(DATASTORE_NAME, StartScreenDataSerializer)

  override val startScreenData: Flow<StartScreenData> =
    context.startScreenDataStore.data


  override suspend fun saveText(text: String) {
    withContext(IO) {
      context.startScreenDataStore.updateData {
        it.copy(text = text)
      }
    }
  }

  override suspend fun showHint() {
    withContext(IO) {
      context.startScreenDataStore.updateData {
        it.copy(appInitStatus = AppInitStatus.SHOW_HINT)
      }
    }
  }

  override suspend fun finishInitialisation() {
    withContext(IO) {
      context.startScreenDataStore.updateData {
        it.copy(appInitStatus = AppInitStatus.INITIALISED)
      }
    }
  }

  companion object {
    private const val DATASTORE_NAME = "start_screen_datastore.json"
  }
}
