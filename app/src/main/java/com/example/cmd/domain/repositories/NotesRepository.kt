package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.MyNoteDomain
import com.example.cmd.domain.entities.NotesSortOrder
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
  val notesDb : Flow<List<MyNoteDomain>>
  suspend fun searchDatabase(searchQuery: String)
  suspend fun changeSortOrder(sortOrder: NotesSortOrder)
  suspend fun deleteMyNote(id: Int)
  suspend fun insertMyNote(title: String, text: String)
  suspend fun edit(myNoteDomain: MyNoteDomain)
}
