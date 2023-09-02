package com.example.cmd.presentation.bindingAdapters

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cmd.presentation.states.DeletionSettingsState

@BindingAdapter("controlProgressVisibility")
fun ProgressBar.controlProgressVisibility(state: DeletionSettingsState) {
  this.visibility = if (state is DeletionSettingsState.Loading) {
    View.VISIBLE
  } else {
    View.GONE
  }
}

@BindingAdapter("controlFilesVisibility")
fun RecyclerView.controlFilesVisibility(state: DeletionSettingsState) {
  this.visibility = if (state is DeletionSettingsState.Loading) {
    View.GONE
  } else {
    View.VISIBLE
  }
}

