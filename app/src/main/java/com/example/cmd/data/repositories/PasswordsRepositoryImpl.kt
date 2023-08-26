package com.example.cmd.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.example.cmd.data.serializers.PasswordsSerializer
import com.example.cmd.domain.entities.Passwords
import com.example.cmd.domain.repositories.PasswordsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PasswordsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
  PasswordsRepository {

  private val Context.passwordsDataStore by dataStore(DATASTORE_NAME, PasswordsSerializer)


  override suspend fun setMainPassword(password: String) {
    withContext(IO) {
      context.passwordsDataStore.updateData {
        it.copy(mainPass = password)
      }
    }
  }

  override suspend fun setSettingsPassword(password: String) {
    withContext(IO) {
      context.passwordsDataStore.updateData {
        it.copy(settingsPass = password)
      }
    }
  }

  override suspend fun getPasswords(): Passwords =
    context.passwordsDataStore.data.first()

  companion object {
    private const val DATASTORE_NAME = "passwords_datastore.json"
  }
}
