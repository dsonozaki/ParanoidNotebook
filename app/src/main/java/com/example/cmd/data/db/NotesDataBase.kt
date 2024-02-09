package com.example.cmd.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MyFileDbModel::class], version = 1)
abstract class NotesDataBase: RoomDatabase() {
  abstract fun myNoteDao(): MyNoteDAO

  companion object {

    private const val DB_NAME = "my_database"
    fun create(context: Context): NotesDataBase {

      return Room.databaseBuilder(
        context,
        NotesDataBase::class.java,
        DB_NAME
      ).build()
    }
  }
}

