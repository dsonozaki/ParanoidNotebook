package com.example.cmd.domain.entities

import kotlinx.serialization.Serializable


@Serializable
data class LogsData( val logsEnabled: Boolean=false,val lastDayOfAutoDeletion: Long = 0L,val logsAutoRemovePeriod: Int = 7, val logDates: List<String> = listOf())
