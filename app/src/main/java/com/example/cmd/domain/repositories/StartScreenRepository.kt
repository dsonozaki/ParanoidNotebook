package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.StartScreenData
import kotlinx.coroutines.flow.Flow

interface StartScreenRepository {
  val startScreenData: Flow<StartScreenData>
  suspend fun saveText(text: String)
  suspend fun showHint()
  suspend fun finishInitialisation()
}
