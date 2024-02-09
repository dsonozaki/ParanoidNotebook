package com.example.cmd.domain.usecases.notesDatabase

import com.example.cmd.domain.entities.MyNoteDomain
import com.example.cmd.domain.repositories.NotesRepository
import javax.inject.Inject

class EditNoteUseCase @Inject constructor(private val repository: NotesRepository) {
  suspend operator fun invoke(myNoteDomain: MyNoteDomain) {
    repository.edit(myNoteDomain)
  }
}
