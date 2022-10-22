package com.example.cmd.db

import androidx.room.*

@Dao
interface ShredDao {
  @Query("SELECT * FROM MyFile")
  fun getAll(): List<MyFile>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(files: List<MyFile>)

  @Delete
  fun delete(file: MyFile)
}
