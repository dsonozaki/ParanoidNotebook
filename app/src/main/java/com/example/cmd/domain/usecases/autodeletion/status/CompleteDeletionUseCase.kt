package com.example.cmd.domain.usecases.autodeletion.status

import com.example.cmd.domain.repositories.DeletionStatusRepository
import javax.inject.Inject

class CompleteDeletionUseCase @Inject constructor(private val repository: DeletionStatusRepository) {
  suspend operator fun invoke() {
    repository.completeDeletion()
  }
}
