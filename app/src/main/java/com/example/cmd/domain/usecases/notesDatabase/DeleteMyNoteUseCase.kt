package com.example.cmd.domain.usecases.notesDatabase

import com.example.cmd.domain.repositories.NotesRepository
import javax.inject.Inject

class DeleteMyNoteUseCase @Inject constructor(private val repository: NotesRepository) {
  suspend operator fun invoke(id: Int) {
    repository.deleteMyNote(id)
  }
}
