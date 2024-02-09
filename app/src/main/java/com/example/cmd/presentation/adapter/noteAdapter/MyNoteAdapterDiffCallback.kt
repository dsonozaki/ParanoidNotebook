package com.example.cmd.presentation.adapter.noteAdapter

import androidx.recyclerview.widget.DiffUtil
import com.example.cmd.domain.entities.MyNoteDomain
import javax.inject.Inject

class MyNoteAdapterDiffCallback @Inject constructor(): DiffUtil.ItemCallback<MyNoteDomain>() {
  override fun areItemsTheSame(oldItem: MyNoteDomain, newItem: MyNoteDomain): Boolean =
    oldItem.id == newItem.id


  override fun areContentsTheSame(oldItem: MyNoteDomain, newItem: MyNoteDomain): Boolean =
    oldItem == newItem
}
