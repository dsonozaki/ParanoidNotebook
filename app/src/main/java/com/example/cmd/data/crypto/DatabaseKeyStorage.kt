package com.example.cmd.data.crypto

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.inject.Inject

class DatabaseKeyStorage @Inject constructor(@ApplicationContext private val context: Context,
private val cryptoManager: CryptoManager,
) {

  private val Context.dataBaseKeyDataStore by preferencesDataStore(DATASTORE_NAME)



  private fun generateRandomKey(): ByteArray =
    ByteArray(32).apply {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        SecureRandom.getInstanceStrong().nextBytes(this)
      } else {
        SecureRandom().nextBytes(this)
      }
    }

  private fun ByteArray.toHex(): String {
    val result = StringBuilder()
    forEach {
      val octet = it.toInt()
      val firstIndex = (octet and 0xF0).ushr(4)
      val secondIndex = octet and 0x0F
      result.append(hexChars[firstIndex])
      result.append(hexChars[secondIndex])
    }
    return result.toString()
  }

  private fun createNewKey(): String {
    val rawByteKey = generateRandomKey()
    val hexKey = rawByteKey.toHex()
    return cryptoManager.encryptString(hexKey)
  }

   suspend fun getDbKey(): CharArray {
      context.dataBaseKeyDataStore.data.map {
        it[PREFERENCES_KEY]
      }.first()?.let {
        return  it.toCharArray()
      }

      val dbKey = createNewKey()
      context.dataBaseKeyDataStore.edit {
        it[PREFERENCES_KEY] = dbKey
      }
      return dbKey.toCharArray()
  }

  companion object {
    private const val DATASTORE_NAME = "database_password.json"
    private val PREFERENCES_KEY = stringPreferencesKey("db_key")
    private val hexChars = "0123456789ABCDEF".toCharArray()
  }

}
