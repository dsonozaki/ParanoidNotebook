package com.example.cmd.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class AutoDeletionData(val isActive: Boolean = false, val timeOut: Int = 0, val xiaomiPhoneNotificationRequired: Boolean)
