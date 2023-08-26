package com.example.cmd.domain.repositories

import com.example.cmd.domain.entities.StartScreenData

interface StartScreenRepository {
  suspend fun saveText(text: String)
  suspend fun showHint()
  suspend fun finishInitialisation()
  suspend fun getStartScreenData(): StartScreenData
}
