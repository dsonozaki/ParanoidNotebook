package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.PasswordsSerializer
import com.example.cmd.domain.entities.Passwords
import com.example.cmd.domain.repositories.PasswordsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PasswordsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, serializer: PasswordsSerializer) :
  PasswordsRepository {

  private val Context.passwordsDataStore by dataStore(DATASTORE_NAME,serializer)

  override suspend fun setMainPassword(password: String) {
      context.passwordsDataStore.updateData {
        it.copy(mainPass = password)
      }
  }

  override suspend fun setSettingsPassword(password: String) {
      context.passwordsDataStore.updateData {
        it.copy(settingsPass = password)
      }
  }

  override fun getPasswordsFlow(): Flow<Passwords> =
    context.passwordsDataStore.data

  companion object {
    private const val DATASTORE_NAME = "passwords_datastore.json"
  }
}
