package com.example.cmd.viewmodel

import android.text.Spanned
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cmd.model.PreferencesModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.File

class LogsViewModel(private val model: PreferencesModel) : ViewModel() {
  var day = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
  var text: MutableLiveData<Spanned> = MutableLiveData()
  val visibility: MutableLiveData<Int> = MutableLiveData() //видимость прогрессбара
  val scroll: MutableLiveData<Boolean> = MutableLiveData() //скроллинг
  init {
    loadLogs()
  }

  fun getTimeLag() = model.getLong("logs_autoremove")

  fun loadLogs() {
    visibility.value = View.VISIBLE
    var result = ""
    //открытие сегодняшних логов
    File("${model.getFilesDir()}/Log/$day").bufferedReader().use {
      var time = ""
      var message = ""
      it.forEachLine {
        val groups =
          Regex("(\\d\\d:\\d\\d:\\d\\d) (.*)").find(
            it
          )?.groupValues
        if (groups != null) {
          time = groups[1]
          message = groups[2]
        } else
          message += it
        //дешифровка и окраска логов
        if (message.endsWith(" C")) {
          message = try {
            "<span style=\"color: #ff0000;\">" + model.decodeString(
              message.removeSuffix(" C")
            ) + "</span>"
          } catch (e: Exception) {
            "<span style=\"color: #ff0000;\">Не удалось расшифровать строку</span>"
          }
          result += "<span style=\"color: #ffcc00;\">$time</span> $message<br>"
        }
        if (message.endsWith(" E")) {
          message = message.removeSuffix(" E")
          result += "<span style=\"color: #ffcc00;\">$time</span> $message<br>"
        }
      }
    }
    text.value = HtmlCompat.fromHtml(result, HtmlCompat.FROM_HTML_MODE_LEGACY)
    visibility.value = View.GONE
    scroll.value = true
  }

  fun clearLogs() {
    File("${model.getFilesDir()}/Log/$day").bufferedWriter().use { it.write("") }
  }

  fun logTimeout(days: Long) {
    model.putLong("logs_autoremove", days)
  }

  //получение всех файлов с логами
  fun getAllowed() =
    File("${model.getFilesDir()}/Log").listFiles()?.map { it.name }?.toSet() ?: setOf()


}
