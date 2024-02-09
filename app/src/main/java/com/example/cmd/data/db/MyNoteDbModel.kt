package com.example.cmd.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
//data class для базы данных Room, хранит сведения о файлах, которые должны удаляться
data class MyNoteDbModel(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  @ColumnInfo val title: String,
  @ColumnInfo val text: String,
  @ColumnInfo val date: Long
)
