package com.example.cmd.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.cmd.domain.entities.MyFileDomain

class MyFileAdapterDiffCallback: DiffUtil.ItemCallback<MyFileDomain>() {
  override fun areItemsTheSame(oldItem: MyFileDomain, newItem: MyFileDomain): Boolean =
    oldItem.path == newItem.path


  override fun areContentsTheSame(oldItem: MyFileDomain, newItem: MyFileDomain): Boolean =
    oldItem == newItem
}
