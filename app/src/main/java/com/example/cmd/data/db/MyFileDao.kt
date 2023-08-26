package com.example.cmd.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MyFileDao {
  @Query("SELECT * FROM MyFileDbModel ORDER BY path ASC")
  fun getDataSortedByPathAsc(): Flow<List<MyFileDbModel>>

  @Query("SELECT * FROM MyFileDbModel ORDER BY path DESC")
  fun getDataSortedByPathDesc(): Flow<List<MyFileDbModel>>

  @Query("SELECT * FROM MyFileDbModel ORDER BY size ASC")
  fun getDataSortedBySizeAsc(): Flow<List<MyFileDbModel>>

  @Query("SELECT * FROM MyFileDbModel ORDER BY size DESC")
  fun getDataSortedBySizeDesc(): Flow<List<MyFileDbModel>>

  @Query("SELECT * FROM MyFileDbModel ORDER BY priority ASC")
  fun getDataSortedByPriorityAsc(): Flow<List<MyFileDbModel>>

  @Query("SELECT * FROM MyFileDbModel ORDER BY priority DESC")
  fun getDataSortedByPriorityDesc(): Flow<List<MyFileDbModel>>

  @Upsert
  suspend fun upsert(file: MyFileDbModel)

  @Query("UPDATE MyFileDbModel SET priority=:priority WHERE uri=:uri")
  suspend fun changePriority(priority: Int, uri: String)

  @Query("DELETE FROM MyFileDbModel WHERE uri=:uri")
  suspend fun delete(uri:String)

  @Query("DELETE FROM MyFileDbModel")
  suspend fun clearDb()
}
