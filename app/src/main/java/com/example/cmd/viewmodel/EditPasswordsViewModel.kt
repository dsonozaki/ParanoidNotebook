package com.example.cmd.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.helpers.Request
import com.example.cmd.model.PreferencesModel
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditPasswordsViewModel(
  private val model: PreferencesModel
) : ViewModel() {
  val main: MutableLiveData<String> = MutableLiveData()
  val settings: MutableLiveData<String> = MutableLiveData()
  val color: MutableLiveData<Boolean> = MutableLiveData() //изменение цвета кнопок
  val back: MutableLiveData<Boolean> = MutableLiveData() //выход из окна
  val visibility: MutableLiveData<Int> = MutableLiveData() //видимость кнопки "назад"
  val toastMessage: MutableLiveData<Request> = MutableLiveData()
  private var paswordsReady = false //пароли
  private var paswordsDeprecated: Boolean = false //пароли введены, но они некорректны
  private var isNotEmpty: Boolean = false //пароли введены
  private lateinit var settingsPassword: String
  private lateinit var mainPassword: String
  private lateinit var settingsPasswordCopy: String
  private lateinit var mainPasswordCopy: String

  init {
    viewModelScope.launch(IO) {
      isNotEmpty = model.getBoolean("notEmptyPasswords")
      if (isNotEmpty) {
        viewModelScope.launch(IO) {
          settingsPassword = model.getPassword("settingsPass")
        }
        mainPassword = model.getPassword("mainPass")
      } else {
        withContext(Main) {
          visibility.value = View.GONE
          color.value = false
        }
      }
    }
  }

  //реакция на введение паролей
  fun changed() {
    //пароли корректные, если главный пароль не содержит в себе пароль от настроек
    if (!isNotEmpty) {
      paswordsReady =
        when (!main.value.isNullOrBlank() && !settings.value.isNullOrBlank() && !main.value!!.contains(
          settings.value!!
        )) {
          true -> {
            color.value = true
            true

          }
          false -> {
            color.value = false
            false
          }
        }
      return
    }
    mainPasswordCopy = when {
      main.value.isNullOrBlank() -> mainPassword
      else -> main.value!!
    }
    settingsPasswordCopy = when {
      settings.value.isNullOrBlank() -> settingsPassword
      else -> settings.value!!
    }
    when (mainPasswordCopy.contains(settingsPasswordCopy)) {
      true -> {
        color.value = false
        paswordsDeprecated = true
      }
      false -> {
        color.value = true
        paswordsDeprecated = false
      }
    }
  }

  //кнопка "далее"
  fun next() {
    //первая установка паролей
    if (!isNotEmpty && paswordsReady) {
      viewModelScope.launch(Default) {
        model.setPassword("mainPass", main.value)
      }
      viewModelScope.launch(Default) {
        model.setPassword("settingsPass", settings.value)
      }

      viewModelScope.launch(Default) {
        model.putBoolean("notEmptyPasswords", true)
      }
      viewModelScope.launch(Default) {
        model.putBoolean("hint", true)
      }
      back()
      return
    }
    //изменение паролей
    if (isNotEmpty) {
      if (!paswordsDeprecated) {
        if (!main.value.isNullOrBlank())
          viewModelScope.launch(Default) {
            model.setPassword("mainPass", main.value)
          }
        if (!settings.value.isNullOrBlank())
          viewModelScope.launch(Default) {
            model.setPassword("settingsPass", settings.value)
          }
        back()
        return
      }
      toastMessage.value =
        Request(Request.Type.TOAST, listOf(mainPasswordCopy, settingsPasswordCopy))

    }

  }


  fun back() {
    back.value = isNotEmpty
  }

}
