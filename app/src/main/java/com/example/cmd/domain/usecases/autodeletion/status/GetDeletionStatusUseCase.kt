package com.example.cmd.domain.usecases.autodeletion.status

import com.example.cmd.domain.entities.DeletionStatus
import com.example.cmd.domain.repositories.DeletionStatusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeletionStatusUseCase @Inject constructor(private val repository: DeletionStatusRepository) {
  operator fun invoke(): Flow<DeletionStatus> = repository.deletionStatus
}
