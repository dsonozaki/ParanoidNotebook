package com.example.cmd.domain.usecases.notesDatabase

import com.example.cmd.domain.repositories.NotesRepository
import javax.inject.Inject

class InsertNoteUseCase @Inject constructor(private val repository: NotesRepository) {
  suspend operator fun invoke(title: String, text: String) {
    repository.insertMyNote(title, text)
  }
}
