package com.example.cmd

import android.content.Context
import android.util.TypedValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.formatDate(): String = this.toLocalDate().toString()

fun LocalDateTime.getMillis() = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDateTime.getEpochDays() = this.toLocalDate().toEpochDay()

fun LifecycleOwner.launchLifecycleAwareCoroutine(coroutine: suspend () -> Unit) {
  with(this) {
    this.lifecycleScope.launch {
      this@with.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
        coroutine()
      }
    }
  }
}

fun Context.getColorForAttribute(attributeId: Int): Int {
  val color = TypedValue()
  this.theme?.resolveAttribute(attributeId,color,true)
  return color.resourceId
}

