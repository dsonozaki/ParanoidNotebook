package com.example.cmd.di

import android.content.Context
import com.example.cmd.data.repositories.AutoDeletionDataRepositoryImpl
import com.example.cmd.data.repositories.DeletionStatusRepositoryImpl
import com.example.cmd.data.repositories.FilesRepositoryImpl
import com.example.cmd.data.repositories.LogsDataRepositoryImpl
import com.example.cmd.data.repositories.LogsRepositoryImpl
import com.example.cmd.domain.entities.LogEntity
import com.example.cmd.domain.repositories.AutoDeletionDataRepository
import com.example.cmd.domain.repositories.DeletionStatusRepository
import com.example.cmd.domain.repositories.FilesRepository
import com.example.cmd.domain.repositories.LogsDataRepository
import com.example.cmd.domain.repositories.LogsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Calendar
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class  SingletonRepositoriesModule {
  @Binds
  @Singleton
  abstract fun bindLogsRepository(logsRepositoryImpl: LogsRepositoryImpl): LogsRepository

  @Binds
  @Singleton
  abstract fun bindLogsDataRepository(logsDataRepositoryImpl: LogsDataRepositoryImpl): LogsDataRepository

  @Binds
  @Singleton
  abstract fun bindFilesRepository(filesRepositoryImpl: FilesRepositoryImpl): FilesRepository

  @Binds
  @Singleton
  abstract fun bindAutoDeletionDataRepository(deletionDataRepositoryImpl: AutoDeletionDataRepositoryImpl): AutoDeletionDataRepository

  @Binds
  @Singleton
  abstract fun bindDeletionStatusRepository(bindDeletionStatusRepositoryImpl: DeletionStatusRepositoryImpl): DeletionStatusRepository

  companion object {
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
      return CoroutineScope(Dispatchers.Default)
    }

    @Provides
    @LogsDirectory
    fun getLogsDirQualifier(@ApplicationContext context: Context): String = "${context.filesDir.path}/Log"

    @Provides
    fun provideTodayCalendar(): Calendar = Calendar.getInstance()

    @Provides
    @Singleton
    fun provideLogEntitySharedFlow(): MutableSharedFlow<LogEntity> = MutableSharedFlow()
  }
}
