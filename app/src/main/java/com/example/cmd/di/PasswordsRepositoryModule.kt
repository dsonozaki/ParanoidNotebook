package com.example.cmd.di

import com.example.cmd.data.repositories.PasswordsRepositoryImpl
import com.example.cmd.domain.repositories.PasswordsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton

@InstallIn(ViewModelComponent::class)
@Module
abstract class PasswordsRepositoryModule {
  @Binds
  @Singleton
  abstract fun bindPasswordsRepository(passwordsRepositoryImpl: PasswordsRepositoryImpl): PasswordsRepository
}
