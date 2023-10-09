package com.example.cmd.domain.repositories

import android.net.Uri
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.domain.entities.MyFileDomain
import kotlinx.coroutines.flow.Flow

interface FilesRepository {
  val filesDb : Flow<List<MyFileDomain>>
  suspend fun clearDb()
  suspend fun changeSortOrder(sortOrder: FilesSortOrder)
  suspend fun changeFilePriority(priority: Int, uri: Uri)
  suspend fun insertMyFile(uri: Uri, isDirectory: Boolean)
  suspend fun deleteMyFile(uri: Uri)

}
