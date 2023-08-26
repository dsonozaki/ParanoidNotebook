package com.example.cmd.presentation.states

import com.example.cmd.domain.entities.AutoDeletionData
import com.example.cmd.domain.entities.MyFileDomain


sealed class DeletionSettingsState {
  object Loading : DeletionSettingsState()
  class ViewData(val autoDeletionData: AutoDeletionData, val data: List<MyFileDomain>)
}
