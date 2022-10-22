package com.example.cmd.viewmodel

import android.os.Environment
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.R
import com.example.cmd.helpers.Request
import com.example.cmd.helpers.StringResource
import com.example.cmd.model.PreferencesModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom

//viewmodel основного экрана
class MainViewModel(
  private val model: PreferencesModel,
  private val stringResource: StringResource
) : ViewModel() {
  val text: MutableLiveData<String> = MutableLiveData() //текст в блокноте
  val action: MutableLiveData<Request> = MutableLiveData() //передача событий к view
  val menu: MutableLiveData<Boolean> =
    MutableLiveData(false)  //режим маскировки/режим paranoid notebook
  val visibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE) //видимость прогрессбара
  val invisibility: MutableLiveData<Int> = MutableLiveData(View.GONE) //видимость текстового поля
  private var makeToast = true //произошла смена режима маскировки?
  private var settingsPassword = ""
  private var mainPassword = ""
  private var hint = false //показывать ли стартовую подсказку?
  private val filesDir = model.getFilesDir() //директория с данными приложения
  private var day = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
  private val alphabet by lazy { ('a'..'z') + ('A'..'Z') + ('0'..'9') }


  init {
    //очистка устаревших логов
    viewModelScope.launch(IO) {
      model.clearLogs()
    }

    viewModelScope.launch(IO) {
      val passwordsReady = model.getBoolean("notEmptyPasswords")
      //первый запуск приложения
      if (!passwordsReady) {
        withContext(Main) {
          action.value = Request(Request.Type.MOVE)
        }
        //сохранение корневой директории
        val root = Environment.getExternalStorageDirectory().path
        model.putString(
          "root",
          root
        )
        //сохранение версиии Android SDK
        viewModelScope.launch(IO) {
          model.putLong("version", android.os.Build.VERSION.SDK_INT.toLong())
        }
        //создание директории для логов
        viewModelScope.launch(IO) {
          model.putLong("logs_autoremove", 7)
          try {
            Files.createDirectory(Paths.get("$filesDir/Log"))
          } catch (e: java.nio.file.FileAlreadyExistsException) {
          }
        }
        //генерация пароля для логов
        viewModelScope.launch(IO) {
          val secureRandom = SecureRandom()
          val ironKey =
            List(16) { alphabet[secureRandom.nextInt(alphabet.lastIndex)] }.joinToString(
              ""
            )
          model.setPassword("ironKey", ironKey)
        }
        //генерация вектора инициализации
        viewModelScope.launch(IO) {
          val secureRandom = SecureRandom()
          val init = List(16) { alphabet[secureRandom.nextInt(alphabet.lastIndex)] }.joinToString(
            ""
          )
          model.setPassword("init", init)
        }
        return@launch
      }
      hint = model.getBoolean("hint")
      //создание файла логов
      File("$filesDir/Log/$day").createNewFile()
      viewModelScope.launch(IO) {
        val accessGranted = model.getBoolean("accessGranted")
        if (!accessGranted) {
          withContext(Main) {
            action.value = Request(Request.Type.ACCESS)
          }
        }
      }
      //подсказка после создания паролей
      if (hint) {
        model.putBoolean("hint", false)
        withContext(Main) {
          text.value = HtmlCompat.fromHtml(
            stringResource.getString(R.string.hint),
            HtmlCompat.FROM_HTML_MODE_LEGACY
          ).toString()
        }
        return@launch
      }
      //обычная инициализация, показ сохранённого текста
      val notepad = model.getString("notepadText")
      withContext(Main) {
        if (notepad.isNotBlank())
          text.value = notepad
      }

    }
  }

  //обновление паролей, окончание загрузки экрана
  fun passwords() {
    viewModelScope.launch(IO) {
      val job1 = viewModelScope.launch(IO) { mainPassword = model.getPassword("mainPass") }
      val job2 = viewModelScope.launch(IO) {
        settingsPassword = model.getPassword("settingsPass")
      }
      joinAll(job1, job2)
      withContext(Main) {
        visibility.value = View.INVISIBLE
        invisibility.value = View.VISIBLE
      }
    }
  }

  //включение/выключение маскировки
  fun changed() {
    if (visibility.value == View.VISIBLE) return
    menu.value =
      when (text.value!!.lines()[0].contains(settingsPassword)) {
        true -> {
          if (makeToast) {
            action.value =
              Request(
                Request.Type.TOAST,
                listOf(stringResource.getString(R.string.secret_hello))
              )
            model.writeLog(false, R.string.enter)
            makeToast = false
          }

          true
        }
        false -> {
          makeToast = true
          false
        }
      }
    if (text.value!!.lines()[0].contains(mainPassword)) {
      viewModelScope.launch(IO) {
        protectData()
      }
    }
  }

  //посыл сигнала о предотвращении удаления
  private suspend fun protectData() {
    val active = model.getBoolean("started")
    if (!active) return
    //данные удалены?
    val isDeleted = model.getLong("isDeleted")
    if (isDeleted > System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime()) {
      withContext(Main) {
        action.value = Request(
          Request.Type.TOAST,
          listOf(stringResource.getString(R.string.too_late))
        )
      }
      return
    }
    //удаление началось?
    model.putCurrentTime("stop")
    val isStarted = model.getBoolean("isStarted")
    if (isStarted) {
      model.putCurrentTime("stop")
      withContext(Main) {
        action.value = Request(
          Request.Type.TOAST,
          listOf(stringResource.getString(R.string.part_saved))
        )

      }
      return
    }
    //данные спасены
    withContext(Main) {
      action.value =
        Request(Request.Type.TOAST, listOf(stringResource.getString(R.string.full_saved)))
    }
  }

  fun accessGranted() {
    model.putBoolean("accessGranted", true)
  }

  //сохранение данных после закрытия экрана
  fun save() {
    if (!hint) {
      viewModelScope.launch(IO) {
        model.putString(
          "notepadText",
          (text.value ?: "").replace(mainPassword, "").replace(settingsPassword, "")
        )
      }
    }
  }


}
