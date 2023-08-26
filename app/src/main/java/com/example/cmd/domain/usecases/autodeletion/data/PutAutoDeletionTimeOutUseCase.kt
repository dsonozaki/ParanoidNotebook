package com.example.cmd.domain.usecases.autodeletion.data

import com.example.cmd.domain.repositories.AutoDeletionDataRepository
import javax.inject.Inject

class PutAutoDeletionTimeOutUseCase @Inject constructor(private val repository: AutoDeletionDataRepository) {
  suspend operator fun invoke(timeout: Int) {
    repository.putAutoDeletionTimeout(timeout)
  }
}
