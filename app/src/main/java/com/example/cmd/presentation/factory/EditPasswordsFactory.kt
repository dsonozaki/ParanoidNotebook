package com.example.cmd.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmd.model.PreferencesModel
import com.example.cmd.presentation.viewmodels.EditPasswordsVM

//factory для viewmodel экрана настроек паролей.
class EditPasswordsFactory(context: Context) : ViewModelProvider.Factory {

  private val model by lazy { PreferencesModel(context) }
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return EditPasswordsVM(model) as T
  }
}
