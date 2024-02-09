package com.example.cmd.di

import android.content.Context
import com.example.cmd.data.db.MyNoteDAO
import com.example.cmd.data.db.NotesDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NotesDataBaseModule {
  @Provides
  @Singleton
  fun provideChannelDao(notesDataBase: NotesDataBase): MyNoteDAO {
    return notesDataBase.myNoteDao()
  }

  @Provides
  @Singleton
  fun provideAppDatabase(
    @ApplicationContext context: Context,
  ): NotesDataBase {
    return NotesDataBase.create(context)
  }
}
