package com.example.cmd.domain.usecases.logs

import com.example.cmd.domain.repositories.LogsRepository
import javax.inject.Inject

class GetLogsDataUseCase @Inject constructor(private val repository: LogsRepository) {
  operator fun invoke() = repository.logsData

}
