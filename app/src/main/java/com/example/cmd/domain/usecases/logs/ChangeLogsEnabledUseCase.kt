package com.example.cmd.domain.usecases.logs

import com.example.cmd.domain.repositories.LogsRepository
import javax.inject.Inject

class ChangeLogsEnabledUseCase @Inject constructor(private val repository: LogsRepository) {
  suspend operator fun invoke() {
    repository.changeLogsEnabled()
  }
}
