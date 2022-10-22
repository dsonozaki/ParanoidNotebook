package com.example.cmd.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.example.cmd.R
import com.example.cmd.databinding.ListRowBinding
import com.example.cmd.db.MyFile
import com.example.cmd.viewmodel.DeletionSettingsVM

//Адаптер для Recycler View
class DataAdapter(
  val viewModel: DeletionSettingsVM
) : RecyclerView.Adapter<DataAdapter.ItemsViewHolder>() {
  lateinit var data: SortedList<MyFile>


  class ItemsViewHolder(val binding: ListRowBinding) :
    RecyclerView.ViewHolder(binding.root)


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ListRowBinding.inflate(inflater, parent, false)
    return ItemsViewHolder(binding)
  }

  //Наполнение строк Recycler View содержимым
  override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
    val file = data[position]
    with(holder.binding) {
      val uriPath = Uri.parse(file.path)
      viewModel.setImage(imageView2, uriPath, file.name)
      path.text = file.name
      priority.text = viewModel.stringSource.getString(R.string.priority, file.priority)
      size.text = convertToSize(file.size)
      delete.setOnClickListener {
        when (val pos = holder.adapterPosition) {
          -1 -> viewModel.frequentDeletion(path.text.toString())
          else -> viewModel.removeItem(pos)
        }
      }
      edit.setOnClickListener {
        val pos = holder.adapterPosition
        viewModel.editItem(pos, path.text.toString(), file.priority.toString())
      }
      imageView2.setOnClickListener {
        viewModel.aboutFile(
          path.text.toString(),
          size.text.toString(),
          priority.text.toString()
        )
      }
    }
  }


  override fun getItemCount() = data.size()


  private fun convertToSize(size: Long): String {
    var number = size
    val names = listOf("KБ", "МБ", "ГБ", "ТБ", "ПБ")
    var i = 0
    while (number > 1023) {
      number /= 1024
      i++
    }
    return (viewModel.stringSource.getString(R.string.size, number.toInt(), names[i]))
  }

}
