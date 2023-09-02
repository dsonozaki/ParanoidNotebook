package com.example.cmd.di

import android.content.Context
import com.example.cmd.data.crypto.DatabaseKeyStorage
import com.example.cmd.data.db.FileDataBase
import com.example.cmd.data.db.MyFileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
class FileDataBaseModule {
  @Provides
  @Singleton
  fun provideChannelDao(fileDataBase: FileDataBase): MyFileDao {
    return fileDataBase.myFileDao()
  }

  @Provides
  @Singleton
  fun provideAppDatabase(
    @ApplicationContext context: Context,
    databaseKeyStorage: DatabaseKeyStorage
  ): FileDataBase {
    val dbKey = runBlocking {  databaseKeyStorage.getDbKey() }
    return FileDataBase.create(context, dbKey)
  }
}
