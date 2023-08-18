package com.example.cmd.di

import com.example.cmd.data.repositories.StartScreenRepositoryImpl
import com.example.cmd.domain.repositories.StartScreenRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton

@InstallIn(ViewModelComponent::class)
@Module
abstract class StartScreenRepositoryModule {
  @Binds
  @Singleton
  abstract fun bindStartScreenRepository(startScreenRepositoryImpl: StartScreenRepositoryImpl): StartScreenRepository

}
