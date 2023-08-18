package com.example.cmd.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class DeletionStatus(
  val deletionState: DeletionState = DeletionState.NOT_STARTED,
  val deletionPreventionTimestamp: Long = 0
)

enum class DeletionState {
  NOT_STARTED, STARTED, COMPLETE
}
