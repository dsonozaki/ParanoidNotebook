package com.example.cmd.domain.usecases.notesDatabase

import com.example.cmd.domain.repositories.NotesRepository
import javax.inject.Inject

class GetNotesDbUseCase @Inject constructor(private val repository: NotesRepository){
  operator fun invoke() = repository.notesDb
}
