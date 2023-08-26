package com.example.cmd.data.serializers

import androidx.datastore.core.Serializer
import com.example.cmd.domain.entities.LogsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object LogsDataSerializer: Serializer<LogsData> {

  override val defaultValue: LogsData
    get() = LogsData()


  override suspend fun readFrom(input: InputStream): LogsData {
    return try {
      Json.decodeFromString(deserializer = LogsData.serializer(),
        string = input.readBytes().decodeToString())
    } catch (e: SerializationException) {
      defaultValue
    }
  }

  override suspend fun writeTo(t: LogsData, output: OutputStream) {
    withContext(Dispatchers.IO) {
      output.write(
        Json.encodeToString(
          serializer = LogsData.serializer(),
          value = t
        ).encodeToByteArray()
      )
    }
  }


}
