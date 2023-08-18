package com.example.cmd.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmd.helpers.DirectoryFileHelper
import com.example.cmd.helpers.StringResource
import com.example.cmd.model.MySortedList
import com.example.cmd.model.PreferencesModel
import com.example.cmd.presentation.viewmodels.DeletionSettingsVM

//factory для viewmodel экрана настроек удаления файлов.
class DeletionFactory(context: Context) : ViewModelProvider.Factory {
  private val model by lazy { MySortedList(context) }
  private val prefmodel by lazy { PreferencesModel(context) }
  private val stringSource by lazy { StringResource(context) }
  private val UriPathHelper by lazy { DirectoryFileHelper(context) }
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return DeletionSettingsVM(model, prefmodel, stringSource, UriPathHelper) as T
  }
}
