package com.example.cmd.domain.usecases.database

import android.net.Uri
import com.example.cmd.domain.repositories.FilesRepository
import javax.inject.Inject

class ChangeFilePriorityUseCase @Inject constructor(private val repository: FilesRepository) {
  suspend operator fun invoke(priority: Int, uri: Uri) {
    repository.changeFilePriority(priority, uri)
  }
}
