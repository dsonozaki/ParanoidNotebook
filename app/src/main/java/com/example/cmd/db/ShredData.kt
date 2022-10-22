package com.example.cmd.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MyFile::class], version = 1)
abstract class ShredData : RoomDatabase() {
  abstract fun shredDao(): ShredDao
}
