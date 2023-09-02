package com.example.cmd.di

import android.content.Context
import com.example.cmd.data.repositories.AutoDeletionDataRepositoryImpl
import com.example.cmd.data.repositories.DeletionStatusRepositoryImpl
import com.example.cmd.data.repositories.FilesRepositoryImpl
import com.example.cmd.data.repositories.LogsDataRepositoryImpl
import com.example.cmd.data.repositories.LogsRepositoryImpl
import com.example.cmd.data.repositories.LogsTextRepositoryImpl
import com.example.cmd.data.repositories.PasswordsRepositoryImpl
import com.example.cmd.data.repositories.StartScreenRepositoryImpl
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.domain.entities.LogEntity
import com.example.cmd.domain.repositories.AutoDeletionDataRepository
import com.example.cmd.domain.repositories.DeletionStatusRepository
import com.example.cmd.domain.repositories.FilesRepository
import com.example.cmd.domain.repositories.LogsDataRepository
import com.example.cmd.domain.repositories.LogsRepository
import com.example.cmd.domain.repositories.LogsTextRepository
import com.example.cmd.domain.repositories.PasswordsRepository
import com.example.cmd.domain.repositories.StartScreenRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class  SingletonRepositoriesModule {
  @Binds
  @Singleton
  abstract fun bindLogsTextRepository(logsTextRepositoryImpl: LogsTextRepositoryImpl): LogsTextRepository
  @Binds
  @Singleton
  abstract fun bindStartScreenRepository(startScreenRepositoryImpl: StartScreenRepositoryImpl): StartScreenRepository
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
  abstract fun bindDeletionStatusRepository(deletionStatusRepositoryImpl: DeletionStatusRepositoryImpl): DeletionStatusRepository

  @Binds
  @Singleton
  abstract fun bindLogsRepository(logsRepositoryImpl: LogsRepositoryImpl): LogsRepository
  @Binds
  @Singleton
  abstract fun bindPasswordsRepository(passwordsRepositoryImpl: PasswordsRepositoryImpl): PasswordsRepository
  companion object {
    @Provides
    @IOCoroutineScope
    fun provideIOCoroutineScope(): CoroutineScope {
      return CoroutineScope(Dispatchers.IO)
    }

    @Provides
    @LogsDirectory
    fun provideLogsDir(@ApplicationContext context: Context): String = "${context.filesDir.path}/Log"

    @Provides
    @Singleton
    fun provideLogEntitySharedFlow(): MutableSharedFlow<LogEntity> = MutableSharedFlow()

    @Provides
    @NewLogFileChannel
    @Singleton
    fun provideLogFileChannel(): Channel<String> = Channel()

    @Provides
    @Singleton
    fun provideSortOrderFlow(): MutableStateFlow<FilesSortOrder> = MutableStateFlow(FilesSortOrder.PRIORITY_DESC)
  }
}
