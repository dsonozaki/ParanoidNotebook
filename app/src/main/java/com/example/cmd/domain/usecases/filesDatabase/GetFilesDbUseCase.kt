package com.example.cmd.domain.usecases.filesDatabase

import com.example.cmd.domain.repositories.FilesRepository
import javax.inject.Inject

class GetFilesDbUseCase @Inject constructor(private val repository: FilesRepository){
  operator fun invoke() = repository.filesDb
}
