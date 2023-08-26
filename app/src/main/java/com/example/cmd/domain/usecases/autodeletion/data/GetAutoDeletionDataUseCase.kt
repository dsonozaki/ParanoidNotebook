package com.example.cmd.domain.usecases.autodeletion.data

import com.example.cmd.domain.entities.AutoDeletionData
import com.example.cmd.domain.repositories.AutoDeletionDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAutoDeletionDataUseCase @Inject constructor(private val repository: AutoDeletionDataRepository) {
  operator fun invoke(): Flow<AutoDeletionData> = repository.autoDeletionData
}
