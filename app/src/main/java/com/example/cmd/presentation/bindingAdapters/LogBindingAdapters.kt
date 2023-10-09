package com.example.cmd.presentation.bindingAdapters

import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.databinding.BindingAdapter
import com.example.cmd.presentation.states.LogsDataState

@BindingAdapter("progressBarFromLogsState")
fun ProgressBar.statusFromState(state: LogsDataState) {
  visibility = when (state) {
    is LogsDataState.Loading -> View.VISIBLE
    is LogsDataState.ViewLogs -> View.GONE
  }
}

@BindingAdapter("logsTextFromState")
fun TextView.textFromState(state: LogsDataState) {
  when (state) {
    is LogsDataState.Loading -> visibility = View.GONE
    is LogsDataState.ViewLogs -> {
      visibility = View.VISIBLE
      text = HtmlCompat.fromHtml(state.logs.asString(context), FROM_HTML_MODE_LEGACY)
    }
  }
}

@BindingAdapter("scrollOnNewItem")
fun ScrollView.scrollOnNewItem(state: LogsDataState) {
  if (state is LogsDataState.ViewLogs) {
    post {
      run {
        fullScroll(View.FOCUS_DOWN);
      }
    }
  }
}
