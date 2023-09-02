package com.example.cmd.data.repositories

import com.example.cmd.data.crypto.CryptoManager
import com.example.cmd.di.LogsDirectory
import com.example.cmd.di.NewLogFileChannel
import com.example.cmd.domain.entities.LogEntity
import com.example.cmd.domain.entities.LogState
import com.example.cmd.domain.repositories.LogsTextRepository
import com.example.cmd.formatDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class LogsTextRepositoryImpl @Inject constructor(
  @LogsDirectory private val logsDirectory: String,
  private val newLogs: MutableSharedFlow<LogEntity>,
  private val cryptoManager: CryptoManager,
  @NewLogFileChannel private val _newFileNotification: Channel<String>
) :
  LogsTextRepository {

  private var logsDirectoryInitialized = File(logsDirectory).exists()

  override val newFileNotification: Flow<String> = _newFileNotification.receiveAsFlow()

  override val logEntity: Flow<LogEntity> = flow {
    val currentDateTime = LocalDateTime.now()
    initializeDirectory()
    if (!File("$logsDirectory/${currentDateTime.formatDate()}").exists()) {
      emit(LogEntity(currentDateTime, "", LogState.CURRENT_LOG_FILE))
    } else {
      emit(
        LogEntity(
          currentDateTime,
          readFromFile(currentDateTime.formatDate()),
          LogState.CURRENT_LOG_FILE
        )
      )
    }
    var watchingOldLogs = false
    newLogs.collect {
      when (it.logState) {
        LogState.CURRENT_LOG_FILE -> {
          emit(it)
          watchingOldLogs = false
        }

        LogState.NEW_LOG_STRING -> {
          if (!watchingOldLogs) {
            emit(it)
          }
        }

        LogState.WATCHING_OLD_LOGS -> {
          emit(it)
          watchingOldLogs = true
        }
      }
    }
  }


  private suspend fun initializeDirectory() {
    if (!logsDirectoryInitialized) {
      withContext(Dispatchers.IO) {
        File(logsDirectory).mkdir()
      }
      logsDirectoryInitialized = true
    }
  }

  enum class StringStatus {
    ENCRYPTED_END, DECRYPTED_END, STRING_BODY
  }

  private fun stringStatus(string: String): StringStatus {
    if (string.endsWith(NORMAL_STRING)) {
      return StringStatus.DECRYPTED_END
    }
    if (string.endsWith(STRING_ENCODED)) {
      return StringStatus.ENCRYPTED_END
    }
    return StringStatus.STRING_BODY
  }

  private fun readFromFile(date: String): String {
    var message = ""
    return buildString {
      File("$logsDirectory/$date").bufferedReader().readLines().forEach {
        when (stringStatus(it)) {
          StringStatus.STRING_BODY -> message += it
          StringStatus.ENCRYPTED_END -> {
            message += it.dropLast(STRING_ENCODED.length+1)
            message = cryptoManager.decryptString(message)
            appendLine(message)
            message = ""
          }

          StringStatus.DECRYPTED_END -> {
            message += it.dropLast(NORMAL_STRING.length+1)
            appendLine(message)
            message = ""
          }
        }
      }
    } //?
  }

  override suspend fun clearLogsForDays(days: Set<String>) {
    days.forEach {
      clearLogsForDay(it)
    }
  }


  private suspend fun initLogFile(dateTime: LocalDateTime): Boolean {
    val day = dateTime.formatDate()
    val file = File("$logsDirectory/$day")
    if (!file.exists()) {
      withContext(Dispatchers.IO) {
        file.createNewFile()
        _newFileNotification.send(day)
      }
      return true
    }
    return false
  }


  override suspend fun clearLogsForDay(day: String) {
    File("$logsDirectory/$day").delete()
    checkOldLogs(day).let {
      if (it == LogState.CURRENT_LOG_FILE) {
        newLogs.emit(LogEntity(LocalDateTime.now(), "", it))
      }
    }
  }

  private fun checkOldLogs(day: String): LogState = if (day == LocalDateTime.now().formatDate()) {
    LogState.CURRENT_LOG_FILE
  } else {
    LogState.WATCHING_OLD_LOGS
  }

  override suspend fun readLogsForDay(localDateTime: LocalDateTime) {
    val day = localDateTime.formatDate()
    val logState = checkOldLogs(day)
    newLogs.emit(LogEntity(localDateTime, readFromFile(day), logState))
  }

  private suspend fun checkForNewFile(dateTime: LocalDateTime): LogState {
    initializeDirectory()
    return if (initLogFile(dateTime)) {
      LogState.CURRENT_LOG_FILE
    } else {
      LogState.NEW_LOG_STRING
    }
  }

  private fun BufferedWriter.writeToLogs(toWrite: String, encrypted: Boolean) {
    this.use {
      if (encrypted) {
        val encryptedString = cryptoManager.encryptString(toWrite)
        it.appendLine("$encryptedString $STRING_ENCODED")
      } else {
        it.appendLine("$toWrite $NORMAL_STRING")
      }
    }
  }


  private suspend fun writeLogsString(string: String, encrypted: Boolean) {
    val currentDateTime = LocalDateTime.now()
    val logState = checkForNewFile(currentDateTime)
    val writer = withContext(Dispatchers.IO) {
      FileOutputStream(
        File("$logsDirectory/${currentDateTime.formatDate()}"),
        true
      ).bufferedWriter()
    }
    val toWrite = java.time.LocalTime.now().format(
      DateTimeFormatter.ofPattern("HH:mm:ss")
    ) + ' ' + string
    writer.writeToLogs(toWrite, encrypted)
    newLogs.emit(LogEntity(currentDateTime, toWrite, logState))
  }


  override suspend fun writeToLogs(string: String) {
    writeLogsString(string, false)
  }

  override suspend fun writeToLogsEncrypted(string: String) {
    writeLogsString(string, true)
  }

  companion object {
    const val STRING_ENCODED = "ENCODED"
    const val NORMAL_STRING = "NORMAL"
  }

}
