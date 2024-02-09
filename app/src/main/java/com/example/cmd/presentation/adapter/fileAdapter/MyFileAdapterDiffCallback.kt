package com.example.cmd.presentation.adapter.fileAdapter

import androidx.recyclerview.widget.DiffUtil
import com.example.cmd.domain.entities.MyFileDomain
import javax.inject.Inject

class MyFileAdapterDiffCallback @Inject constructor(): DiffUtil.ItemCallback<MyFileDomain>() {
  override fun areItemsTheSame(oldItem: MyFileDomain, newItem: MyFileDomain): Boolean =
    oldItem.name == newItem.name


  override fun areContentsTheSame(oldItem: MyFileDomain, newItem: MyFileDomain): Boolean =
    oldItem == newItem
}
