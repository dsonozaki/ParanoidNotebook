package com.example.cmd

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun LocalDateTime.formatDate(): String = this.toLocalDate().toString()

fun LocalDateTime.getMillis() = this.toInstant(ZoneOffset.of(ZoneId.systemDefault().id)).toEpochMilli()

fun LocalDateTime.getEpochDays() = this.toLocalDate().toEpochDay()
