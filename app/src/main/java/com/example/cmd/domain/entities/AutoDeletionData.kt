package com.example.cmd.domain.entities

import com.example.cmd.presentation.states.DeletionActivationStatus
import kotlinx.serialization.Serializable

@Serializable
data class AutoDeletionData(val isActive: Boolean = false, val timeOut: Int = 0, val xiaomiPhoneNotificationRequired: Boolean) {
  fun toDeletionActivationStatus(): DeletionActivationStatus {
    if (this.isActive) {
      return DeletionActivationStatus.ACTIVE
    }
    if (this.timeOut == 0) {
      return DeletionActivationStatus.INACTIVE_AND_WITHOUT_TIMEOUT
    }
    if (this.xiaomiPhoneNotificationRequired) {
      return DeletionActivationStatus.INACTIVE_AND_NOT_NOTIFIED_XIAOMI
    }
    return DeletionActivationStatus.INACTIVE_AND_READY
  }
}


