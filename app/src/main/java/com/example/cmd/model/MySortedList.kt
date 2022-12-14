package com.example.cmd.model

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import androidx.room.Room
import com.anggrayudi.storage.extension.isTreeDocumentFile
import com.example.cmd.adapter.DataAdapter
import com.example.cmd.db.MyFile
import com.example.cmd.db.ShredDao
import com.example.cmd.db.ShredData
import net.sqlcipher.database.SupportFactory

//DataCallback для вычисления изменений в списках
class DataCallback(
  private val oldList: List<MyFile>,
  private val newList: List<MyFile>,
  private val priority: MySortedList.Priority
) : DiffUtil.Callback() {
  override fun getOldListSize(): Int = oldList.size


  override fun getNewListSize(): Int = newList.size

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
    oldList[oldItemPosition].path == newList[newItemPosition].path


  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val oldItem = oldList[oldItemPosition]
    val newItem = newList[newItemPosition]
    return when (priority) {
      MySortedList.Priority.PATH_ASCENDING, MySortedList.Priority.PATH_DESCENDING ->
        oldItem.path == newItem.path
      MySortedList.Priority.PRIORITY_DESCENDING, MySortedList.Priority.PRIORITY_ASCENDING ->
        oldItem.priority == newItem.priority
      MySortedList.Priority.SIZE_ASCENDING, MySortedList.Priority.SIZE_DESCENDING ->
        oldItem.size == newItem.size
    }
  }

}

//Класс для хранения данных о файлах, которые должны быть удалены
class MySortedList(private val applicationContext: Context) {
  lateinit var adapter: DataAdapter
  lateinit var data: SortedList<MyFile> //основной, обновляемый список файлов
  private var initialized = false
  lateinit var priorityType: Priority
  private lateinit var fileDao: ShredDao
  private var items: List<MyFile> = listOf()

  //установка сортировки по приоритету
  fun init() {
    switchPriority(Priority.PRIORITY_DESCENDING)
    initialized = true
  }

  //виды сортировки
  enum class Priority {
    PATH_ASCENDING, PATH_DESCENDING, SIZE_DESCENDING, SIZE_ASCENDING, PRIORITY_ASCENDING, PRIORITY_DESCENDING
  }

  //добавление файла в data
  fun add(file: MyFile): Boolean {
    return if (data.indexOf(file) == -1) {
      data.add(file)
      true
    } else
      false
  }

  //загрузка списка файлов в data и items
  fun loadList(key: String) {
    items = getList(key)
    data.addAll(items.filter {
      val uri = Uri.parse(it.path)
      if (uri.isTreeDocumentFile) {
        DocumentFile.fromTreeUri(applicationContext, Uri.parse(it.path))!!.exists()
      } else {
        DocumentFile.fromSingleUri(applicationContext, Uri.parse(it.path))!!.exists()
      }
    })
  }

  //Скачивание списка файлов из базы данных
  fun getList(key: String): List<MyFile> {
    val db = Room.databaseBuilder(
      applicationContext,
      ShredData::class.java, "ShredData"
    ).openHelperFactory(SupportFactory(key.toByteArray())).build()
    fileDao = db.shredDao()
    return fileDao.getAll()
  }

  //удаление файла из базы данных
  fun delete(file: MyFile) {
    fileDao.delete(file)
  }

  //Сравение списка data со списком items, обновление базы данных
  fun upLoadList() {
    val newList = getList()
    val toRemove = items.toSet().minus(newList)
    for (element in toRemove) {
      fileDao.delete(element)
    }
    fileDao.insertAll(newList)
  }

  //преобразование data в список
  private fun getList(): List<MyFile> {
    val list = mutableListOf<MyFile>()
    for (i in 0 until data.size()) {
      list.add(data[i])
    }
    return list
  }

  fun clear() {
    data.clear()
  }

  //изменение принципа сортировки элементов. Вычисление изменений, отправка в адаптер RecyclerView.
  fun switchPriority(priority: Priority) {
    val files = when (initialized) {
      true -> getList()
      false -> listOf()
    }
    data = SortedList(
      MyFile::class.java,
      object : SortedListAdapterCallback<MyFile>(adapter) {
        override fun compare(o1: MyFile, o2: MyFile): Int =
          when (priority) {
            Priority.PATH_DESCENDING ->
              o2.name.lowercase().compareTo(o1.name.lowercase())
            Priority.PATH_ASCENDING ->
              -o2.name.lowercase().compareTo(o1.name.lowercase())
            Priority.PRIORITY_DESCENDING ->
              o2.priority - o1.priority
            Priority.PRIORITY_ASCENDING ->
              o1.priority - o2.priority
            Priority.SIZE_ASCENDING ->
              (o1.size - o2.size).toInt()
            Priority.SIZE_DESCENDING ->
              (o2.size - o1.size).toInt()
          }

        override fun areContentsTheSame(
          oldItem: MyFile,
          newItem: MyFile
        ): Boolean = when (priority) {
          Priority.PATH_ASCENDING, Priority.PATH_DESCENDING ->
            oldItem.path == newItem.path
          Priority.PRIORITY_DESCENDING, Priority.PRIORITY_ASCENDING ->
            oldItem.priority == newItem.priority
          Priority.SIZE_ASCENDING, Priority.SIZE_DESCENDING ->
            oldItem.size == newItem.size
        }

        override fun areItemsTheSame(
          item1: MyFile,
          item2: MyFile
        ): Boolean = item1.path == item2.path

      })
    priorityType = priority
    data.addAll(files)
    adapter.data = data
    val dataCallback = DataCallback(files, getList(), priority)
    val diff = DiffUtil.calculateDiff(dataCallback)
    diff.dispatchUpdatesTo(adapter)

  }
}
