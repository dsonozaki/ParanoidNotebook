package com.example.cmd.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import coil.load
import com.example.cmd.R
import com.example.cmd.databinding.ListRowBinding
import com.example.cmd.domain.entities.FileType
import com.example.cmd.domain.entities.MyFileDomain
import javax.inject.Inject

//Адаптер для Recycler View
class MyFileAdapter @Inject constructor(
  diffCallback: MyFileAdapterDiffCallback,
) : ListAdapter<MyFileDomain,MyFileViewHolder>(diffCallback) {

  var onItemLongClickListener: ((MyFileDomain) -> Unit)? = null
  var onEditItemClickListener: ((MyFileDomain) -> Unit)? = null
  var onDeleteItemClickListener: ((Uri) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFileViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ListRowBinding.inflate(inflater, parent, false)
    return MyFileViewHolder(binding)
  }

  private fun ImageView.setImage(fyleType: FileType, uri: Uri) {
      when(fyleType) {
       FileType.DIRECTORY -> this.load(R.drawable.ic_baseline_folder_24_colored)
        FileType.IMAGE -> this.load(uri)
        FileType.USUAL_FILE -> this.load(R.drawable.ic_baseline_insert_drive_file_color)
      }
  }

  //Наполнение строк Recycler View содержимым
  override fun onBindViewHolder(holder: MyFileViewHolder, position: Int) {
    val file = getItem(position)
    with(holder.binding) {
      imageView2.setImage(file.fileType, file.uri)
      path.text = file.name
      priority.text = root.context.getString(R.string.priority, file.priority)
      size.text = root.context.getString(R.string.size,file.sizeFormatted)
      delete.setOnClickListener {
        onDeleteItemClickListener?.invoke(file.uri)
      }
      edit.setOnClickListener {
        onEditItemClickListener?.invoke(file)
      }
      imageView2.setOnClickListener {
        onItemLongClickListener?.invoke(file)
      }
    }
  }

}
