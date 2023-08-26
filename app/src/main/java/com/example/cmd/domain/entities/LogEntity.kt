package com.example.cmd.domain.entities

import java.time.LocalDateTime

data class LogEntity(val today : LocalDateTime, val logs: String, val logState: LogState)
