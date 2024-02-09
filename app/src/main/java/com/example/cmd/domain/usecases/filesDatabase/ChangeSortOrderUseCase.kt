package com.example.cmd.domain.usecases.filesDatabase

import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.domain.repositories.FilesRepository
import javax.inject.Inject

class ChangeSortOrderUseCase @Inject constructor(private val repository: FilesRepository){
  suspend operator fun invoke(sortOrder: FilesSortOrder) {
    repository.changeSortOrder(sortOrder)
  }
}
