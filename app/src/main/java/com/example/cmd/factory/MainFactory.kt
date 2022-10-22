package com.example.cmd.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmd.helpers.StringResource
import com.example.cmd.model.PreferencesModel
import com.example.cmd.viewmodel.MainViewModel

//factory для viewmodel основного экрана.
class MainFactory(context: Context) : ViewModelProvider.Factory {
  private val model by lazy { PreferencesModel(context) }
  private val stringSource by lazy { StringResource(context) }
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return MainViewModel(model, stringSource) as T
  }
}
