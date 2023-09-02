package com.example.cmd.data.repositories

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getAbsolutePath
import com.anggrayudi.storage.file.mimeType
import com.example.cmd.data.db.MyFileDao
import com.example.cmd.data.db.MyFileDbModel
import com.example.cmd.data.mappers.MyFileMapper
import com.example.cmd.domain.entities.FileType
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.domain.entities.MyFileDomain
import com.example.cmd.domain.repositories.FilesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class FilesRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val myFileDao: MyFileDao,
  private val mapper: MyFileMapper,
  private val sortOrderFlow: MutableStateFlow<FilesSortOrder>
) : FilesRepository {

  @OptIn(ExperimentalCoroutinesApi::class)
  override val filesDb : Flow<List<MyFileDomain>> = sortOrderFlow.flatMapLatest {
    when (it) {
      FilesSortOrder.NAME_ASC -> myFileDao.getDataSortedByPathAsc().map { mapper.mapDbListToDtList(it) }
      FilesSortOrder.NAME_DESC -> myFileDao.getDataSortedByPathDesc().map {mapper.mapDbListToDtList(it) }
      FilesSortOrder.SIZE_ASC -> myFileDao.getDataSortedBySizeAsc().map { mapper.mapDbListToDtList(it) }
      FilesSortOrder.SIZE_DESC -> myFileDao.getDataSortedBySizeDesc().map { mapper.mapDbListToDtList(it) }
      FilesSortOrder.PRIORITY_ASC -> myFileDao.getDataSortedByPriorityAsc().map { mapper.mapDbListToDtList(it) }
      FilesSortOrder.PRIORITY_DESC -> myFileDao.getDataSortedByPriorityDesc().map { mapper.mapDbListToDtList(it) }
    }
  }

  override suspend fun clearDb() {
    myFileDao.clearDb()
  }

  override suspend fun changeFilePriority(priority: Int, uri: Uri) {
    myFileDao.changePriority(priority, uri.toString())
  }

  override suspend fun changeSortOrder(sortOrder: FilesSortOrder) {
    sortOrderFlow.emit(sortOrder)
  }

  //Получение размера файла или папки
  private fun getFileSize(path: String): Long {
    val process = Runtime.getRuntime().exec(arrayOf("du", "-s", path))
    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
    bufferedReader.use {
      val line = it.readLine()
      val result = try {
        line.split("\t")[0].toLong()
      } catch (e: Exception) {
        0
      }
      return result
    }
  }

  private fun Long.convertToHumanFormat(): String {
    var number = this
    val names = listOf("KB", "MB", "GB", "TB")
    var i = 0
    while (number > 1023) {
      number /= 1024
      i++
    }
    return "${number.toInt()} ${names[i]}"
  }

  override suspend fun insertMyFile(uri: Uri, isDirectory: Boolean) {
    val df = if(isDirectory) {
      DocumentFile.fromTreeUri(context,uri)
    } else {
      DocumentFile.fromSingleUri(context,uri)
    }?: throw RuntimeException("Can't get file or directory for uri $uri")
    val path = df.getAbsolutePath(context)
    val size = getFileSize(path)
    val fileType = if (isDirectory) {
      FileType.DIRECTORY
    } else {
      if (df.mimeType?.startsWith("image/")==true) {
        FileType.IMAGE
      } else {
        FileType.USUAL_FILE
      }
    }
      myFileDao.upsert(MyFileDbModel(uri = uri.toString(), path = path,size = size, priority = 0, fileType = fileType, sizeFormated = size.convertToHumanFormat()))
  } //?

  override suspend fun deleteMyFile(uri: Uri) {
    myFileDao.delete(uri.toString())
  }

}
