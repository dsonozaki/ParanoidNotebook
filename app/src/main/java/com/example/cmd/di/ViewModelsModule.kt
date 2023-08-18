package com.example.cmd.di

import com.example.cmd.presentation.UIText
import com.example.cmd.presentation.states.LogsScreenState
import com.example.cmd.presentation.states.PasswordsState
import com.example.cmd.presentation.states.StartScreenState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@InstallIn(ViewModelsModule::class)
@Module
class ViewModelsModule {
  @Provides
  @Singleton
  fun provideStartScreenSharedFlow(): MutableStateFlow<StartScreenState> = MutableStateFlow(StartScreenState.Loading)

  @Provides
  @MainScreenNotificationChannel
  @Singleton
  fun provideMainScreenNotificationFlow(): Channel<UIText> = Channel()

  @Provides
  @Singleton
  fun providePasswordsStateFlow() : MutableStateFlow<PasswordsState> = MutableStateFlow(PasswordsState.PasswordsIncorrect())

  @Provides
  @Singleton
  fun provideLogsStateFlow(): MutableStateFlow<LogsScreenState> = MutableStateFlow(LogsScreenState.ViewLogs("",""))
}
