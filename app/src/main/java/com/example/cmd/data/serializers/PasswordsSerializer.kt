package com.example.cmd.data.serializers

import androidx.datastore.core.Serializer
import com.example.cmd.data.crypto.CryptoManager
import com.example.cmd.domain.entities.Passwords
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

object PasswordsSerializer :
  Serializer<Passwords> {

  @Inject
  private lateinit var cryptoManager: CryptoManager

  override val defaultValue: Passwords
    get() = Passwords()


  override suspend fun readFrom(input: InputStream): Passwords {
    val decryptedBytes = cryptoManager.decryptFromFile(input)
    return try {
      Json.decodeFromString(
        deserializer = Passwords.serializer(),
        string = decryptedBytes.decodeToString()
      )
    } catch (e: SerializationException) {
      defaultValue
    }
  }

  override suspend fun writeTo(t: Passwords, output: OutputStream) {
    withContext(Dispatchers.IO) { //?
      cryptoManager.encryptToFile(
        bytes = Json.encodeToString(
          serializer = Passwords.serializer(),
          value = t
        ).encodeToByteArray(),
        outputStream = output
      )
    }
  }
}
