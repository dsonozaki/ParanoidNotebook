package com.example.cmd.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
//data class для базы данных Room, хранит сведения о файлах, которые должны удаляться
data class MyFileDbModel(
    @ColumnInfo val size: Long,
    @PrimaryKey val path: String,
    @ColumnInfo val priority: Int,
    @ColumnInfo val name: String
) {
}
