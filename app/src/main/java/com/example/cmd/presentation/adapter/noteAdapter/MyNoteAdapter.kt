package com.example.cmd.presentation.adapter.noteAdapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import coil.load
import com.example.cmd.R
import com.example.cmd.databinding.NoteCardviewBinding
import com.example.cmd.domain.entities.FileType
import com.example.cmd.domain.entities.MyNoteDomain
import javax.inject.Inject

//Адаптер для Recycler View
class MyNoteAdapter @Inject constructor(
  diffCallback: MyNoteAdapterDiffCallback,
) : ListAdapter<MyNoteDomain, MyNoteViewHolder>(diffCallback) {

  var onMoreClickListener: ((Int) -> Unit)? = null
  var onEditItemClickListener: ((MyNoteDomain) -> Unit)? = null
  var onDeleteItemClickListener: ((Int) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyNoteViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = NoteCardviewBinding.inflate(inflater, parent, false)
    return MyNoteViewHolder(binding)
  }

  private fun ImageView.setImage(fyleType: FileType, uri: Uri) {
      when(fyleType) {
       FileType.DIRECTORY -> this.load(R.drawable.ic_baseline_folder_24_colored)
        FileType.IMAGE -> this.load(uri)
        FileType.USUAL_FILE -> this.load(R.drawable.ic_baseline_insert_drive_file_color)
      }
  }

  //Наполнение строк Recycler View содержимым
  override fun onBindViewHolder(holder: MyNoteViewHolder, position: Int) {
    val note = getItem(position)
    with(holder.binding) {
      title.text = note.title
      text.text = note.text
      delete.setOnClickListener {
        onDeleteItemClickListener?.invoke(note.id)
      }
      edit.setOnClickListener {
        onEditItemClickListener?.invoke(note)
      }
      more.setOnClickListener {
        onMoreClickListener?.invoke(note.id)
      }
    }
  }

}
