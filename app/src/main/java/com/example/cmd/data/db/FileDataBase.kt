package com.example.cmd.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [MyFileDbModel::class], version = 1)
abstract class FileDataBase : RoomDatabase() {
  abstract fun myFileDao(): MyFileDao

  companion object {

    private const val DB_NAME = "my_database"
    fun create(context: Context, password: CharArray): FileDataBase {

      val supportFactory = SupportFactory(SQLiteDatabase.getBytes(password))
      return Room.databaseBuilder(
        context,
        FileDataBase::class.java,
        DB_NAME
      ).openHelperFactory(supportFactory).build()
    }
  }
}
