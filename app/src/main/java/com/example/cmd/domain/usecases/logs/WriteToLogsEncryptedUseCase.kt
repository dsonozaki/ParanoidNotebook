package com.example.cmd.domain.usecases.logs

import com.example.cmd.domain.repositories.LogsRepository
import javax.inject.Inject

class WriteToLogsEncryptedUseCase @Inject constructor(private val repository: LogsRepository) {
  suspend operator fun invoke(string: String) {
    repository.writeToLogsEncrypted(string)
  }

}
