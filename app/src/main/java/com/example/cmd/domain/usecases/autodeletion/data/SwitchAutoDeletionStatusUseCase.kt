package com.example.cmd.domain.usecases.autodeletion.data

import com.example.cmd.domain.repositories.AutoDeletionDataRepository
import javax.inject.Inject

class SwitchAutoDeletionStatusUseCase @Inject constructor(private val repository: AutoDeletionDataRepository) {
  suspend operator fun invoke() {
    repository.switchAutoDeletionStatus()
  }
}
