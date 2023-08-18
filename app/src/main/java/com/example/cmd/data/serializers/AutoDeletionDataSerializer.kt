package com.example.cmd.data.serializers

import androidx.datastore.core.Serializer
import com.example.cmd.domain.entities.AutoDeletionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AutoDeletionDataSerializer: Serializer<AutoDeletionData> {
  override val defaultValue: AutoDeletionData
    get() = AutoDeletionData()

  override suspend fun readFrom(input: InputStream): AutoDeletionData {
    return try {
      Json.decodeFromString(deserializer = AutoDeletionData.serializer(),
        string = input.readBytes().decodeToString())
    } catch (e: SerializationException) {
      defaultValue
    }
  }

  override suspend fun writeTo(t: AutoDeletionData, output: OutputStream) {
    withContext(Dispatchers.IO) {
      output.write(
        Json.encodeToString(
          serializer = AutoDeletionData.serializer(),
          value = t
        ).encodeToByteArray()
      )
    }
  }
}
