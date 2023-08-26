package com.example.cmd.domain.usecases.startScreen

import com.example.cmd.domain.repositories.StartScreenRepository
import javax.inject.Inject

class FinishInitializationUseCase @Inject constructor(private val repository: StartScreenRepository) {
  suspend operator fun invoke() {
    repository.finishInitialisation()
  }
}
