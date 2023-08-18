package com.example.cmd.presentation.viewmodels

import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.load
import com.example.cmd.R
import com.example.cmd.data.db.MyFileDbModel
import com.example.cmd.helpers.DirectoryFileHelper
import com.example.cmd.helpers.Request
import com.example.cmd.helpers.StringResource
import com.example.cmd.model.MySortedList
import com.example.cmd.model.PreferencesModel
import com.example.cmd.presentation.adapter.DataAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

//viewmodel для настроек файлов для удаления
@HiltViewModel
class DeletionSettingsVM(
  private val myList: MySortedList,
  val model: PreferencesModel,
  val stringSource: StringResource,
  val directoryFileHelper: DirectoryFileHelper
) : ViewModel() {


  val started: MutableLiveData<Boolean> = MutableLiveData() //запущено ли автоудаление?
  val loaded: MutableLiveData<DataAdapter> = MutableLiveData() //адаптер RecyclerView
  val back: MutableLiveData<Boolean> = MutableLiveData()
  var timeout: String = "" //тайм-аут до удаления файлов
  val action: MutableLiveData<Request> = MutableLiveData() //передача событий к view
  var position: Int = 0 //позиция элемента в адаптере
  var checkChineseStupidPhone = false //проверено ли устройство на производителя-Xiaomi?
  val visibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE) //видимость прогрессбара
  val invisibility: MutableLiveData<Int> = MutableLiveData(View.GONE) //видимость RecyclerView
  var version = 0L //версия android SDK
  var notified = false

  init {
    viewModelScope.launch(Default) {
      val adapter = DataAdapter(this@DeletionSettingsVM)
      //загрузка модели, связывание её с адаптером
      val job1 = viewModelScope.launch(IO) {
        myList.adapter = adapter
        myList.init()
        adapter.data = myList.data
        myList.loadList(model.getPassword("ironKey"))
      }
      val job2 = viewModelScope.launch(IO) { timeout = model.getString("timeOut") }
      val job3 = viewModelScope.launch(Main) { started.value = model.getBoolean("started") }
      val job4 = viewModelScope.launch(IO) {
        checkChineseStupidPhone = model.getBoolean("chinesePhone")
      }
      val job5 = viewModelScope.launch(IO) {
        version = model.getLong("version")
      }
      val job6 = viewModelScope.launch(IO) {
        notified = model.getBoolean("notified")
      }
      joinAll(job1, job2, job3, job4, job5, job6)
      //завершение загрузки
      withContext(Main) {
        loaded.value = adapter
        visibility.value = View.INVISIBLE
        invisibility.value = View.VISIBLE
      }
    }
  }

  //загрузка изображений в RecyclerView
  fun setImage(imageView: ImageView, path: Uri, name: String) {
    viewModelScope.launch(IO) {
      val isDirectory = directoryFileHelper.isDirectory(path)
      if (isDirectory)
        imageView.load(R.drawable.ic_baseline_folder_24_colored)
      else if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(
          ".jpeg"
        ) || path.toString().endsWith(".bmp")
      )
        imageView.load(path)
      else
        imageView.load(R.drawable.ic_baseline_insert_drive_file_color)
    }
  }

  //окно активации автоудаления
  fun switch() {
    if (started.value!!) {
      started.value = false
      return
    }
    if (timeout.isEmpty()) {
      action.value = Request(
        Request.Type.ALERT,
        listOf(
          stringSource.getString(R.string.please_set_timeout1),
          stringSource.getString(R.string.please_set_timeout2)
        )
      )
      return
    }
    var message =
      stringSource.getString(R.string.final_explanation, timeout)
    when (checkChineseStupidPhone) {
      false -> {
        if ("xiaomi" == (Build.MANUFACTURER).lowercase())
          message += stringSource.getString(R.string.stupid_chinese_phone)
        action.value = Request(
          Request.Type.QUESTION, listOf(
            "chinese",
            stringSource.getString(R.string.are_you_sure_autodelete),
            message
          )
        )
      }
      true -> action.value = Request(
        Request.Type.QUESTION, listOf(
          "start",
          stringSource.getString(R.string.are_you_sure_autodelete),
          message
        )
      )
    }

  }

  //активация автоудаления
  fun launch(isChecked: Boolean = false) {
    if (isChecked) {
      checkChineseStupidPhone = true
      viewModelScope.launch(Default) {
        model.putBoolean("chinesePhone", true)
      }
    }
    viewModelScope.launch(Default) {
      model.putBoolean("started", true)
    }
    started.value = true
  }

  //вывод окна изменения тайм-аута удаления файлов
  fun timer() {
    action.value = Request(
      Request.Type.TIMEOUT,
      listOf(
        stringSource.getString(R.string.timeout_please),
        stringSource.getString(R.string.correction),
        timeout
      )
    )
  }


  fun clear() {
    myList.clear()
  }

  fun frequentDeletion(path: String) {
    action.value = Request(
      Request.Type.TOAST,
      listOf(stringSource.getString(R.string.frequentDeletion, path))
    )
  }

  fun aboutFile(path: String, size: String, priority: String) {
    action.value = Request(
      Request.Type.ALERT,
      listOf(
        stringSource.getString(R.string.about_file_title),
        stringSource.getString(R.string.about_file, path, size, priority)
      )
    )
  }

  //вывод окна изменения приоритета файла
  fun editItem(pos: Int, path: String, priority: String) {
    position = pos
    action.value = Request(
      Request.Type.PRIORITY,
      listOf(
        stringSource.getString(R.string.changePriority),
        stringSource.getString(R.string.file, path),
        priority
      )
    )
  }

  fun removeItem(pos: Int) {
    myList.data.removeItemAt(pos)
  }


  fun sort(priority: MySortedList.Priority) {
    myList.switchPriority(priority)
  }

  //добавление файлов, выбранных в File Picker
  fun addFile(uri: Uri, isMedia: Boolean) {
    val isDirectory = directoryFileHelper.isDirectory(uri)
    val file = directoryFileHelper.toDocumentFile(uri, isDirectory)
    val task1 = viewModelScope.async(IO) {
      if (version < 30 || !isMedia) {
        return@async directoryFileHelper.getPath(uri, isDirectory)
      }
      return@async file.name
    }
    val task2 = viewModelScope.async(IO) {
      if (isDirectory) {
        return@async getSize(directoryFileHelper.getPath(uri, isDirectory) ?: "")
      }
      return@async file.length() / 1024
    }
    runBlocking {
      val (name, size) = awaitAll(task1, task2)
      val added = myList.add(
        (MyFileDbModel(
          size as Long,
          uri.toString(),
          0,
          name as String
        ))
      )
      if (!added)
        action.value = Request(
          Request.Type.TOAST,
          listOf(stringSource.getString(R.string.fileExists, uri.toString()))
        )
    }
  }

  //изменение тайм-аута автоудаления
  fun putTimeout(time: String): Boolean {
    return try {
      time.toUInt()
      timeout = time
      true
    } catch (e: NumberFormatException) {
      action.value = Request(
        Request.Type.TOAST,
        listOf(stringSource.getString(R.string.number_big_limit))
      )
      false
    }
  }

  //Получение размера файла или папки
  private fun getSize(path: String): Long {
    val process = Runtime.getRuntime().exec(arrayOf("du", "-s", path))
    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
    bufferedReader.use {
      val line = it.readLine()
      val result = try {
        line.split("\t")[0].toLong()
      } catch (e: Exception) {
        0
      }
      return result
    }
  }

  //изменение приоритетеа файла
  fun editPriority(priority: String): Boolean {
    try {
      val number = priority.toInt()
      if (number.coerceIn(0, 999) != number) {
        throw NumberFormatException()
      }
    } catch (e: NumberFormatException) {
      action.value = Request(
        Request.Type.TOAST,
        listOf(stringSource.getString(R.string.number_small_limit))
      )
      return false
    }
    with(myList) {
      data.updateItemAt(
        position, MyFileDbModel(
          data[position].size,
          data[position].path,
          priority.toInt(),
          data[position].name
        )
      )
      if (priorityType != MySortedList.Priority.PRIORITY_ASCENDING && priorityType != MySortedList.Priority.PRIORITY_DESCENDING) {
        adapter.notifyItemChanged(position)
      }
    }
    return true
  }

  fun notified() {
    notified = true
    model.putBoolean("notified", true)
  }

  //сохранение данных на выходе
  fun save() {
    viewModelScope.launch(IO) {
      model.putString("timeOut", timeout)
      model.putBoolean("started", started.value!!)
    }
    viewModelScope.launch(IO) {
      myList.upLoadList()
    }
  }

}
