package com.example.cmd.domain.usecases.startScreen

import com.example.cmd.domain.entities.StartScreenData
import com.example.cmd.domain.repositories.StartScreenRepository
import javax.inject.Inject

class GetDataUseCase @Inject constructor(private val repository: StartScreenRepository) {
  suspend operator fun invoke(): StartScreenData {
    return repository.getStartScreenData()
  }
}
