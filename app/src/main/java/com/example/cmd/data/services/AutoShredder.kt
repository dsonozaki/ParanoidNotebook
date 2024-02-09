package com.example.cmd.data.services

import android.content.Context
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.cmd.R
import com.example.cmd.domain.entities.DeletionStatus
import com.example.cmd.domain.entities.FileType
import com.example.cmd.domain.entities.MyFileDomain
import com.example.cmd.domain.usecases.autodeletion.status.CompleteDeletionUseCase
import com.example.cmd.domain.usecases.autodeletion.status.GetDeletionStatusUseCase
import com.example.cmd.domain.usecases.autodeletion.status.StartDeletionUseCase
import com.example.cmd.domain.usecases.filesDatabase.DeleteMyFileUseCase
import com.example.cmd.domain.usecases.filesDatabase.GetFilesDbUseCase
import com.example.cmd.domain.usecases.logs.WriteToLogsEncryptedUseCase
import com.example.cmd.domain.usecases.logs.WriteToLogsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

//основная часть программы.
@HiltWorker
class AutoShredder @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted workerParams: WorkerParameters,
  private val writeToLogsUseCase: WriteToLogsUseCase,
  private val getFilesDbUseCase: GetFilesDbUseCase,
  private val getDeletionStatusUseCase: GetDeletionStatusUseCase,
  private val writeToLogsEncryptedUseCase: WriteToLogsEncryptedUseCase,
  private val deleteMyFileUseCase: DeleteMyFileUseCase,
  private val startDeletionUseCase: StartDeletionUseCase,
  private val completeDeletionUseCase: CompleteDeletionUseCase
) :
  CoroutineWorker(context, workerParams) {
  private var version = 0
  private var filesList = listOf<MyFileDomain>()

  override suspend fun doWork(): Result {
    coroutineScope {
      val job1 = launch {
        writeToLogsUseCase(context.getString(R.string.deletion_launched))
      }
      val job2 = launch {
        version = Build.VERSION.SDK_INT //?
      }
      val job3 = launch {
        filesList = getFilesDbUseCase().first()
      }
      joinAll(job1, job2, job3)
      writeToLogsUseCase(context.getString(R.string.deletion_started))
      removeAll()
    }
    return Result.success()
  }

  private fun MyFileDomain.toDocumentFile(): DocumentFile? {
    return if (fileType == FileType.DIRECTORY) {
      DocumentFile.fromTreeUri(context, uri)
    } else {
      DocumentFile.fromSingleUri(context, uri)
    }
  }

  private suspend fun writeAboutDeletionError(isDirectory: Boolean, name: String, error: String) {
    val id1 = if (isDirectory) {
      R.string.folder_deletion_error
    } else {
      R.string.file_deletion_error
    }
    writeToLogsEncryptedUseCase(
      context.getString(
        id1,
        name, error
      )
    )
  }

  private suspend fun processDeletionResults(
    result: Pair<Int, Int>,
    isDirectory: Boolean,
    file: MyFileDomain
  ) {
    if (isDirectory) {
      if (result.second == 0) {
        deleteMyFileUseCase(file.uri)
        writeToLogsEncryptedUseCase(
          context.getString(
            R.string.folder_deletion_success,
            file.name,
            100
          )
        )
        return
      }
      val percent = result.first / result.second
      if (percent > 0.5) {
        deleteMyFileUseCase(file.uri)
        writeToLogsEncryptedUseCase(
          context.getString(
            R.string.folder_deletion_success,
            file.name,
            percent * 100
          )
        )
        return
      }
      writeToLogsEncryptedUseCase(
        context.getString(
          R.string.folder_deletion_failed,
          file.name,
          percent * 100
        )
      )
      return
    }
    if (result.first == 1) {
      deleteMyFileUseCase(file.uri)
      writeToLogsEncryptedUseCase(
        context.getString(
          R.string.deletion_success,
          file.name
        )
      )
      return
    }
    writeToLogsEncryptedUseCase(
      context.getString(
        R.string.deletion_failed,
        file.name
      )
    )
  }

  private suspend fun removeFile(coroutineScope: CoroutineScope, file: MyFileDomain): Job {
    return coroutineScope.launch(IO) {
      val name = file.name
      val isDirectory = file.fileType == FileType.DIRECTORY
      val id = if (isDirectory) {
        R.string.deletion_folder
      } else {
        R.string.deletion_file
      }
      writeToLogsEncryptedUseCase(
        context.getString(id, name)
      )
      val df = try {
        file.toDocumentFile() ?: throw RuntimeException()
      } catch (e: Exception) {
        writeAboutDeletionError(isDirectory, name, context.getString(R.string.access_error))
        return@launch
      }
      val result: Pair<Int, Int> = deleteFile(df, file.name, isDirectory)
      processDeletionResults(result, isDirectory, file)
    }
  }


  private suspend fun removeAll() {
    coroutineScope {
      if (checkDeletionStatus()) return@coroutineScope
      startDeletionUseCase()
      //Файлы сортируются по приоритетам, файлы с одинаковыми приоритетами удаляются параллельно
      filesList.sortedByDescending { it.priority }.groupBy { it.priority }.forEach { it1 ->
        val jobs: List<Job> = it1.value.map {
          removeFile(this, it)
        }
        if (checkDeletionStatus()) return@coroutineScope
        jobs.joinAll()
      }
      completeDeletionUseCase()
    }
  }

  private suspend fun checkDeletionStatus(): Boolean {
    val deletionStatus =  getDeletionStatusUseCase().first()
    if (deletionStatus is DeletionStatus.Prevented && deletionStatus.isActualState()) {
      writeToLogsUseCase(context.getString(R.string.deletion_declined))
      return true
    }
    return false
  }


  //Удаление файлов на SD-карте. На выходе общее число файлов и число удалённых файлов.
  private suspend fun deleteFile(
    df: DocumentFile,
    path: String,
    isDirectory: Boolean
  ): Pair<Int, Int> {
    if (isDirectory) {
      val resultFiles = mutableListOf<Pair<Int, Int>>()
      val resultDirs = mutableListOf<Deferred<Pair<Int, Int>>>()
      df.listFiles().forEach {
        if (it.isDirectory) {
          resultDirs += coroutineScope { async(IO) { deleteFile(it, it.name ?: "Unknown", true) } }
        } else {
          resultFiles += deleteFile(it, it.name ?: "Unknown", false)
        }
      }
      val result = resultFiles + resultDirs.awaitAll()
      var (success, all) = listOf(0, 0)
      result.forEach { success += it.first; all += it.second }
      if (success / all > 0.5) {
        if (!df.delete()) {
          writeAboutDeletionError(
            true, path,
            "File not deleted"
          )
        }
      }
      return Pair(success, all)
    }
    if (!df.delete()) {
      writeAboutDeletionError(
        false, path,
        "Directory not deleted"
      )
      return Pair(0, 1)
    }
    return Pair(1, 1)
  }

  companion object {
    const val WORK_NAME = "auto_shredder"

    fun start(context: Context) {
      val workManager = WorkManager.getInstance(context)
      workManager.enqueueUniqueWork(WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<AutoShredder>().build()
      )
    }

  }


}
