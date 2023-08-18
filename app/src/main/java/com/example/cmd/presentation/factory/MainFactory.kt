package com.example.cmd.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmd.helpers.StringResource
import com.example.cmd.model.PreferencesModel
import com.example.cmd.presentation.viewmodels.MainVM

//factory для viewmodel основного экрана.
class MainFactory(context: Context) : ViewModelProvider.Factory {
  private val model by lazy { PreferencesModel(context) }
  private val stringSource by lazy { StringResource(context) }
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return MainVM(model, stringSource) as T
  }
}
