package com.example.cmd.data.repositories

import com.example.cmd.data.db.MyFileDao
import com.example.cmd.data.mappers.MyFileMapper
import com.example.cmd.domain.entities.MyFileDomain
import com.example.cmd.domain.repositories.FilesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class FilesRepositoryImpl @Inject constructor(
  private val myFileDao: MyFileDao,
  private val mapper: MyFileMapper,
  private val coroutineScope: CoroutineScope
) : FilesRepository {

  override suspend fun getFiles(): SharedFlow<List<MyFileDomain>> = myFileDao.getAll().map {
    it.map { mapper.mapDbToDtModel((it)) }
  }.shareIn(coroutineScope, SharingStarted.Lazily, replay = 1)

  override suspend fun insertMyFile(myFile: MyFileDomain) {
    myFileDao.upsert(mapper.mapDtToDbModel(myFile))
  }

  override suspend fun deleteMyFile(myFile: MyFileDomain) {
    myFileDao.delete(mapper.mapDtToDbModel(myFile))
  }

}
