package com.example.cmd.domain.usecases.logs

import com.example.cmd.domain.repositories.LogsRepository
import javax.inject.Inject

class WriteToLogsUseCase @Inject constructor(private val repository: LogsRepository) {
  suspend operator fun invoke(string: String) {
    repository.writeToLogs(string)
  }

}
