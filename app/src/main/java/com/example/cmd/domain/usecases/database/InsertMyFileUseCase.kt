package com.example.cmd.domain.usecases.database

import android.net.Uri
import com.example.cmd.domain.repositories.FilesRepository
import javax.inject.Inject

class InsertMyFileUseCase @Inject constructor(private val repository: FilesRepository) {
  suspend operator fun invoke(uri: Uri, isDirectory: Boolean) {
    repository.insertMyFile(uri, isDirectory)
  }
}
