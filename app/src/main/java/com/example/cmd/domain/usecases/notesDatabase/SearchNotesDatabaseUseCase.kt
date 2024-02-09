package com.example.cmd.domain.usecases.notesDatabase

import com.example.cmd.domain.repositories.NotesRepository
import javax.inject.Inject

class SearchNotesDatabaseUseCase @Inject constructor(private val repository: NotesRepository) {
  suspend operator fun invoke(searchQuery: String) {
    repository.searchDatabase(searchQuery)
  }
}
