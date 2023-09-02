package com.example.cmd.presentation.states


sealed class DeletionSettingsState {
  object Loading : DeletionSettingsState()
  class ViewData(val timeout: Int, val status: DeletionActivationStatus) : DeletionSettingsState()
}

enum class DeletionActivationStatus {
  ACTIVE, INACTIVE_AND_READY, INACTIVE_AND_NOT_NOTIFIED_XIAOMI, INACTIVE_AND_WITHOUT_TIMEOUT
}
