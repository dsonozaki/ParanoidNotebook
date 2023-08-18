package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.MyFileDomain
import kotlinx.coroutines.flow.Flow

interface FilesRepository {
  suspend fun getFiles() : Flow<List<MyFileDomain>>
  suspend fun insertMyFile(myFile: MyFileDomain)
  suspend fun deleteMyFile(myFile: MyFileDomain)
}
