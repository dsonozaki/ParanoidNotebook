package com.example.cmd

import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.formatDate(): String = this.toLocalDate().toString()

fun LocalDateTime.getMillis() = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDateTime.getEpochDays() = this.toLocalDate().toEpochDay()

fun MaterialButton.setButtonColor(color: Int) {
  setStrokeColorResource(color)
  setTextColor(ContextCompat.getColor(context, color))
}
