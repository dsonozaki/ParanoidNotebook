package com.example.cmd.data.repositories

import com.example.cmd.data.crypto.CryptoManager
import com.example.cmd.di.LogsDirectory
import com.example.cmd.domain.entities.LogEntity
import com.example.cmd.domain.entities.LogState
import com.example.cmd.domain.repositories.LogsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

class LogsRepositoryImpl @Inject constructor(
  @LogsDirectory private val logsDirectory: String,
  calendar: Calendar,
  private val newLogs: MutableSharedFlow<LogEntity>
) :
  LogsRepository {

  private var logsDirectoryInitialized = File(logsDirectory).exists()

  private var currentDate = calendar.formatDate()

  @Inject
  private lateinit var cryptoManager: CryptoManager

  override val logEntity: Flow<LogEntity> = flow {
    initializeDirectory()
    if (!File("$logsDirectory/$currentDate").exists()) {
      emit(LogEntity(currentDate, "", LogState.NEW_LOG_FILE))
    } else {
      emit(LogEntity(currentDate, readFromFile(currentDate), LogState.NEW_LOG_FILE))
    }
    var watchingOldLogs = false
    newLogs.collect {
      when (it.logState) {
        LogState.NEW_LOG_FILE -> {
          if (!watchingOldLogs) {
            emit(it)
          }
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
        File(logsDirectory).createNewFile()
      }
      logsDirectoryInitialized = true
    }

  }

  private fun readFromFile(date: String): String =
    buildString {
      File("$logsDirectory/$date").bufferedReader().readLines().forEach {
        this.appendLine(readString(it))
      }
    }

  private fun readString(string: String): String {
    if (string.endsWith(NORMAL_STRING)) {
      return string.dropLast(NORMAL_STRING.length)
    }
    return cryptoManager.decryptString(string.dropLast(STRING_ENCODED.length))
  }

  override suspend fun clearLogsForDays(days: Set<String>) {
    days.forEach {
      clearLogsForDay(it)
    }
  }


  private suspend fun initLogFolder(): Boolean {
    if (!File(logsDirectory).exists()) {
      withContext(Dispatchers.IO) {
        Files.createDirectory(Paths.get(logsDirectory))
      }
    }
    return true
  }


  override suspend fun clearLogsForDay(day: String) {
    File("$logsDirectory/${day}").delete()
  }

  private suspend fun checkOldLogs(day: String): LogState = if (day == currentDate) {
    LogState.NEW_LOG_FILE
  } else {
    LogState.WATCHING_OLD_LOGS
  }

  override suspend fun readLogsForDay(day: String) {
    val logState = checkOldLogs(day)
    newLogs.emit(LogEntity(day, readFromFile(day), logState))
  }

  private suspend fun checkForNewFile(): LogState {
    val currentDay = Calendar.getInstance().formatDate()
    return if (currentDay != currentDate) {
      initLogFolder()
      currentDate = currentDay
      LogState.NEW_LOG_FILE
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
    val logState = checkForNewFile()
    val writer = withContext(Dispatchers.IO) {
      FileOutputStream(File("$logsDirectory/$currentDate"), true).bufferedWriter()
    }
    val toWrite = java.time.LocalTime.now().format(
      DateTimeFormatter.ofPattern("HH:mm:ss")
    ) + string
    writer.writeToLogs(toWrite, encrypted)
    newLogs.emit(LogEntity(currentDate, toWrite, logState))
  }


  override suspend fun writeToLogs(string: String) {
    writeLogsString(string, false)
  }

  override suspend fun writeToLogsEncrypted(string: String) {
    writeLogsString(string, true)
  }

  companion object {
    private const val STRING_ENCODED = "ENCODED"
    private const val NORMAL_STRING = "NORMAL"
  }

}
