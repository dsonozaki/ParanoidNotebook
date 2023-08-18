package com.example.cmd.data.serializers

import androidx.datastore.core.Serializer
import com.example.cmd.domain.entities.DeletionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object DeletionStatusSerializer: Serializer<DeletionStatus> {
  override val defaultValue: DeletionStatus
    get() = DeletionStatus()

  override suspend fun readFrom(input: InputStream): DeletionStatus {
    return try {
      Json.decodeFromString(deserializer = DeletionStatus.serializer(),
        string = input.readBytes().decodeToString())
    } catch (e: SerializationException) {
      defaultValue
    }
  }

  override suspend fun writeTo(t: DeletionStatus, output: OutputStream) {
    withContext(Dispatchers.IO) {
      output.write(
        Json.encodeToString(
          serializer = DeletionStatus.serializer(),
          value = t
        ).encodeToByteArray()
      )
    }
  }
}
