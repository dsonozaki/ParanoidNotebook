package com.example.cmd.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao

interface MyNoteDAO {
  @Query("SELECT * FROM MyNoteDbModel ORDER BY title ASC")
  fun getDataSortedByTitleAsc(): Flow<List<MyNoteDbModel>>

  @Query("SELECT * FROM MyNoteDbModel ORDER BY title DESC")
  fun getDataSortedByTitleDesc(): Flow<List<MyNoteDbModel>>

  @Query("SELECT * FROM MyNoteDbModel ORDER BY date ASC")
  fun getDataSortedByDateAsc(): Flow<List<MyNoteDbModel>>

  @Query("SELECT * FROM MyNoteDbModel ORDER BY date DESC")
  fun getDataSortedByDateDesc(): Flow<List<MyNoteDbModel>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(file: MyNoteDbModel)

  @Update
  suspend fun update(note: MyNoteDbModel)

  @Query("DELETE FROM MyNoteDbModel WHERE id=:id")
  suspend fun delete(id:Int)

}
