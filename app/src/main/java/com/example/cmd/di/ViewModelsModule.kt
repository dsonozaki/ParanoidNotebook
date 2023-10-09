package com.example.cmd.di

import com.example.cmd.domain.entities.Passwords
import com.example.cmd.presentation.actions.DeletionSettingsAction
import com.example.cmd.presentation.actions.LogsActions
import com.example.cmd.presentation.actions.StartScreenActions
import com.example.cmd.presentation.states.DeletionDataState
import com.example.cmd.presentation.states.LogsDataState
import com.example.cmd.presentation.states.StartScreenState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@InstallIn(ViewModelComponent::class)
@Module
class ViewModelsModule {
  @Provides
  fun provideStartScreenStateFlow(): MutableStateFlow<StartScreenState> = MutableStateFlow(StartScreenState.Loading)

  @Provides
  fun provideDeletionSettingsActionChannel(): Channel<DeletionSettingsAction> = Channel()

  @Provides
  fun provideMainScreenActionChannel(): Channel<StartScreenActions> = Channel()

  @Provides
  fun provideGoToMainScreenChannel() : Channel<Unit> = Channel()

  @Provides
  fun provideLogsStateFlow(): MutableSharedFlow<LogsDataState> = MutableSharedFlow()

  @Provides
  fun providePasswordsStateFlow(): MutableStateFlow<Passwords> = MutableStateFlow(Passwords())

  @Provides
  fun provideDeletionSettingsStateFlow(): MutableStateFlow<DeletionDataState> = MutableStateFlow(DeletionDataState.Loading)

  @Provides
  fun provideLogsActionChannel(): Channel<LogsActions> = Channel()
}
