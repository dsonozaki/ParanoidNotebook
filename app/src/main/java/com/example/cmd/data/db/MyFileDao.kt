package com.example.cmd.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MyFileDao {
  @Query("SELECT * FROM MyFileDbModel ORDER BY name ASC")
  fun getAll(): Flow<List<MyFileDbModel>>


  @Upsert
  suspend fun upsert(file: MyFileDbModel)

  @Delete
  suspend fun delete(file: MyFileDbModel)
}
