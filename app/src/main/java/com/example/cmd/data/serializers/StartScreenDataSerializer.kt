package com.example.cmd.data.serializers

import androidx.datastore.core.Serializer
import com.example.cmd.domain.entities.StartScreenData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object StartScreenDataSerializer: Serializer<StartScreenData> {
  override val defaultValue: StartScreenData
    get() = StartScreenData()

  override suspend fun readFrom(input: InputStream): StartScreenData {
    return try {
      Json.decodeFromString(deserializer = StartScreenData.serializer(),
      string = input.readBytes().decodeToString())
    } catch (e: SerializationException) {
      defaultValue
    }
  }

  override suspend fun writeTo(t: StartScreenData, output: OutputStream) {
    withContext(Dispatchers.IO) {
      output.write(
        Json.encodeToString(
          serializer = StartScreenData.serializer(),
          value = t
        ).encodeToByteArray()
      )
    }
  }

}
