package com.example.cmd.data.repositories

import android.content.Context
import com.example.cmd.data.db.MyNoteDAO
import com.example.cmd.data.db.MyNoteDbModel
import com.example.cmd.data.mappers.MyNoteMapper
import com.example.cmd.domain.entities.MyNoteDomain
import com.example.cmd.domain.entities.NotesSortOrder
import com.example.cmd.domain.repositories.NotesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val myNoteDAO: MyNoteDAO,
  private val mapper: MyNoteMapper,
  private val sortOrderFlow: MutableStateFlow<NotesSortOrder>,
  private val filterFlow: MutableStateFlow<String>
) : NotesRepository {

  @OptIn(ExperimentalCoroutinesApi::class)
  override val notesDb: Flow<List<MyNoteDomain>> = sortOrderFlow.flatMapLatest {
    val filesFlow = when (it) {
      NotesSortOrder.DATE_ASC -> myNoteDAO.getDataSortedByDateAsc()
      NotesSortOrder.DATE_DESC -> myNoteDAO.getDataSortedByDateDesc()
      NotesSortOrder.TITLE_ASC -> myNoteDAO.getDataSortedByTitleAsc()
      NotesSortOrder.TITLE_DESC -> myNoteDAO.getDataSortedByTitleDesc()
    }
    filesFlow
      .map { it.filter { it.text.contains(filterFlow.value) || it.title.contains(filterFlow.value) } }
      .map { mapper.mapDbListToDtList(it) }
  }

  override suspend fun changeSortOrder(sortOrder: NotesSortOrder) {
    sortOrderFlow.emit(sortOrder)
  }

  override suspend fun edit(myNoteDomain: MyNoteDomain) {
    myNoteDAO.update(mapper.mapDtToDbModel(myNoteDomain))
  }

  override suspend fun insertMyNote(title: String, text: String) {
    val date = Clock.System.now().epochSeconds
    myNoteDAO.upsert(MyNoteDbModel(title = title,text = text,date = date))
  }

  override suspend fun searchDatabase(searchQuery: String) {
    filterFlow.emit(searchQuery)
  }

  override suspend fun deleteMyNote(id: Int) {
    myNoteDAO.delete(id)
  }
}
