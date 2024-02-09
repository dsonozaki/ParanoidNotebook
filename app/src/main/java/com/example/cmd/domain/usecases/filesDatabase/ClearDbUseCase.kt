package com.example.cmd.domain.usecases.filesDatabase

import com.example.cmd.domain.repositories.FilesRepository
import javax.inject.Inject

class ClearDbUseCase @Inject constructor(private val repository: FilesRepository){
  suspend operator fun invoke() {
    repository.clearDb()
  }
}
