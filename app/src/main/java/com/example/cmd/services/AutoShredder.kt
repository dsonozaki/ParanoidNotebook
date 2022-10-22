package com.example.cmd.services

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.anggrayudi.storage.file.isMediaDocument
import com.anggrayudi.storage.file.openOutputStream
import com.example.cmd.R
import com.example.cmd.db.MyFile
import com.example.cmd.helpers.DirectoryFileHelper
import com.example.cmd.model.MySortedList
import com.example.cmd.model.PreferencesModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.File
import java.util.*

//основная часть программы. Стирание данных по алгоритму HMG IS5.
class AutoShredder(context: Context, workerParams: WorkerParameters) :
  Worker(context, workerParams) {
  private lateinit var files: List<MyFile>
  private var rd = Random()
  private val model by lazy { PreferencesModel(context) }
  private val directoryFileHelper by lazy { DirectoryFileHelper(context) }
  private val data by lazy {
    MySortedList(context)
  }
  private var version = 0L

  //массивы для перезаписи данных
  private val bigZeroArray by lazy { ByteArray(10485760) { 0 } }
  private val bigOneArray by lazy { ByteArray(10485760) { 1 } }
  private val bigRandArray by lazy {
    ByteArray(10485760) {
      rd.nextInt(Byte.MAX_VALUE.toInt()).toByte()
    }
  }
  private var day = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

  override fun doWork(): Result {
    GlobalScope.launch(IO) {
      model.clearLogs()
    }
    runBlocking(IO) {
      File("${model.getFilesDir()}/Log/$day").createNewFile()
      model.writeLog(false, R.string.deletion_launched)
      val start = model.getBoolean("started") //запущено ли автоудаление?
      if (!start) {
        model.writeLog(false, R.string.deletion_declined)
        return@runBlocking
      }
      val job1 = launch(IO) {
        model.putBoolean("isStarted", false) //получение данных, удаление файлов не началось
        files = data.getList(model.getPassword("ironKey"))
      }
      val job2 = launch(IO) {
        val timeout = model.getString("timeOut") //тайм-аут до удаления файлов
        model.writeLog(false, R.string.deletion_confirmed, timeout)
        delay(timeout.toLong() * 1000)
      }
      val job3 = launch(IO) {
        version = model.getLong("version")
      }
      joinAll(job1, job2, job3)
      //удаление файлов, удаление файлов началось
      model.putBoolean("isStarted", true)
      model.writeLog(false, R.string.deletion_started)
      removeAll()
    }
    model.putBoolean("isStarted", false)
    model.writeLog(false, R.string.deletion_complete)
    return Result.success()
  }


  private suspend fun removeAll() {
    //Файлы сортируются по приоритетам, файлы с одинаковыми приоритетами удаляются параллельно
    files.sortedByDescending { it.priority }.groupBy { it.priority }.forEach { it1 ->
      val stop =
        model.getLong("stop") //получение времени сигнала о прекращении удаления. Если он был отправлен до ближайшей загрузки устройства, то удаление отменяется.
      if (stop > System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime()) {
        model.writeLog(false, R.string.deletion_declined)
        return
      }
      val jobs: List<Job> = it1.value.map {
        GlobalScope.launch(IO) {
          val path = Uri.parse(it.path)
          val name = it.name
          val isDirectory = directoryFileHelper.isDirectory(path)
          val id = if (isDirectory) {
            R.string.deletion_folder
          } else {
            R.string.deletion_file
          }
          model.writeLog(
            true,
            id,
            name
          )
          val df = try {
            directoryFileHelper.toDocumentFile(path, isDirectory)
          } catch (e: Exception) {
            val id1 = if (isDirectory) {
              R.string.folder_deletion_error
            } else {
              R.string.file_deletion_error
            }
            model.writeLog(
              true,
              id1,
              name,
              applicationContext.getString(R.string.access_error)
            )
            return@launch
          }
          val result: Pair<Int, Int> = if (!df.isMediaDocument || version > 32) {
            HMGIS5(df)
          } else if (version < 30) {
            HMGIS5FILE(File(it.name))
          } else {
            model.writeLog(true, R.string.problematic_file, name)
            return@launch
          }
          //вычисление результативности удаления директории. Если удалено больше половины файлов в папке, удаление считается успешным.
          if (isDirectory) {
            if (result.second == 0) {
              data.delete(it)
              model.writeLog(true, R.string.folder_deletion_success, name, 100)
              return@launch
            }
            val percent = result.first / result.second
            if (percent > 0.5) {
              data.delete(it)
              model.writeLog(true, R.string.folder_deletion_success, name, percent * 100)
              return@launch
            }
            model.writeLog(true, R.string.folder_deletion_failed, name, percent * 100)
            return@launch
          }
          if (result.first == 1) {
            data.delete(it)
            model.writeLog(
              true,
              R.string.deletion_success,
              name
            )
            return@launch
          }
          model.writeLog(
            true,
            R.string.deletion_failed,
            name
          )
        }
      }
      jobs.joinAll()
    }
    model.putCurrentTime("isDeleted") //удаление завершено
  }

  //запись случайных байтов в файл
  private fun random(df: DocumentFile) {
    val length = df.length()
    val len = length.floorDiv(10485760)
    val end = length % 10485760
    df.openOutputStream(applicationContext, false)!!.buffered(2048).use {
      for (i in 0 until len.toInt()) {
        bigRandArray.shuffle()
        it.write(bigRandArray)
      }
      it.write(ByteArray(end.toInt()) { rd.nextInt(Byte.MAX_VALUE.toInt()).toByte() })
    }
  }

  //заполнение файла определённым байтом
  private fun characterFill(df: DocumentFile, char: Byte) {
    val length = df.length()
    val array = if (char.toInt() == 0) {
      bigZeroArray
    } else
      bigOneArray
    val len = length.floorDiv(10485760)
    val end = length % 10485760
    val endArray = ByteArray(end.toInt()) { char }
    df.openOutputStream(applicationContext, false)!!.buffered(2048).use {
      for (i in 0 until len.toInt()) {
        it.write(array)
      }
      it.write(endArray)
    }
  }

  //HMGIS5 для файлов на SD-карте. На выходе общее число файлов и число удалённых файлов.
  private suspend fun HMGIS5(df: DocumentFile): Pair<Int, Int> {
    if (df.isDirectory) {
      val resultFiles = mutableListOf<Pair<Int, Int>>()
      val resultDirs = mutableListOf<Deferred<Pair<Int, Int>>>()
      df.listFiles().forEach {
        if (it.isDirectory) {
          resultDirs += GlobalScope.async(IO) { HMGIS5(it) }
        } else {
          resultFiles += HMGIS5(it)
        }
      }
      val result = resultFiles + resultDirs.awaitAll()
      var (success, all) = listOf(0, 0)
      result.forEach { success += it.first; all += it.second }
      if (success / all > 0.5) {
        try {
          df.delete()
        } catch (e: Exception) {
          model.writeLog(
            true,
            R.string.folder_deletion_error,
            df.name ?: "Unknown",
            e.localizedMessage ?: "Unknown"
          )
        }
      }
      return Pair(success, all)
    }
    try {
      characterFill(df, 0)
      characterFill(df, 1)
      random(df)
      df.delete()
    } catch (e: Exception) {
      model.writeLog(
        true,
        R.string.file_deletion_error,
        df.name ?: "Unknown",
        e.message ?: "Unknown"
      )
      return Pair(0, 1)
    }
    return Pair(1, 1)
  }

  //запись случайных байтов в файл
  private fun randomFile(file: File) {
    val length = file.length()
    val len = length.floorDiv(10485760)
    val end = length % 10485760
    file.outputStream().buffered(2048).use {
      for (i in 0 until len.toInt()) {
        bigRandArray.shuffle()
        it.write(bigRandArray)
      }
      it.write(ByteArray(end.toInt()) { rd.nextInt(Byte.MAX_VALUE.toInt()).toByte() })
    }
  }

  //заполнение файла определённым байтом
  private fun characterFillFile(file: File, char: Byte) {
    val length = file.length()
    val array = if (char.toInt() == 0) {
      bigZeroArray
    } else
      bigOneArray
    val len = length.floorDiv(10485760)
    val end = length % 10485760
    val endArray = ByteArray(end.toInt()) { char }
    file.outputStream().buffered(2048).use {
      for (i in 0 until len.toInt()) {
        it.write(array)
      }
      it.write(endArray)
    }
  }

  //HMGIS5 для файлов. Приходится использовать, если работа с URI невозможна.
  private suspend fun HMGIS5FILE(file: File): Pair<Int, Int> {
    if (file.isDirectory) {
      val resultFiles = mutableListOf<Pair<Int, Int>>()
      val resultDirs = mutableListOf<Deferred<Pair<Int, Int>>>()
      file.listFiles()?.forEach {
        if (it.isDirectory) {
          resultDirs += GlobalScope.async(IO) { HMGIS5FILE(it) }
        } else {
          resultFiles += HMGIS5FILE(it)
        }
      }
      val result = resultFiles + resultDirs.awaitAll()
      var (success, all) = listOf(0, 0)
      result.forEach { success += it.first; all += it.second }
      try {
        file.delete()
      } catch (e: Exception) {
        model.writeLog(
          true,
          R.string.folder_deletion_error,
          file.path,
          e.message ?: "Unknown"
        )
      }
      return Pair(success, all)
    }
    try {
      characterFillFile(file, 0)
      characterFillFile(file, 1)
      randomFile(file)
      file.delete()
    } catch (e: Exception) {
      model.writeLog(
        true,
        R.string.file_deletion_error,
        file.path,
        e.message ?: "Unknown"
      )
      return Pair(0, 1)
    }
    return Pair(1, 1)
  }


}
