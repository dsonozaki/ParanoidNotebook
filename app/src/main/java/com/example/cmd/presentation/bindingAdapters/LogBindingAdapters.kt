package com.example.cmd.presentation.bindingAdapters

import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.databinding.BindingAdapter
import com.example.cmd.presentation.states.LogsScreenState

@BindingAdapter("progressBarFromLogsState")
fun ProgressBar.statusFromState(state: LogsScreenState) {
  visibility = when (state) {
    is LogsScreenState.Loading -> View.VISIBLE
    is LogsScreenState.ViewLogs -> View.GONE
  }
}

@BindingAdapter("logsTextFromState")
fun TextView.textFromState(state: LogsScreenState) {
  when (state) {
    is LogsScreenState.Loading -> visibility = View.GONE
    is LogsScreenState.ViewLogs -> {
      visibility = View.VISIBLE
      text = HtmlCompat.fromHtml(state.logs.asString(context), FROM_HTML_MODE_LEGACY)
    }
  }
}

@BindingAdapter("scrollOnNewItem")
fun ScrollView.scrollOnNewItem(state: LogsScreenState) {
  if (state is LogsScreenState.ViewLogs) {
    fullScroll(ScrollView.FOCUS_DOWN)
  }
}
