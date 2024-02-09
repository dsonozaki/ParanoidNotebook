package com.example.cmd.domain.usecases.notesDatabase

import com.example.cmd.domain.entities.NotesSortOrder
import com.example.cmd.domain.repositories.NotesRepository
import javax.inject.Inject

class ChangeSortOrderUseCase @Inject constructor(private val repository: NotesRepository){
  suspend operator fun invoke(sortOrder: NotesSortOrder) {
    repository.changeSortOrder(sortOrder)
  }
}
