package com.example.cmd.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmd.model.PreferencesModel
import com.example.cmd.viewmodel.LogsViewModel
//factory для viewmodel экрана просмотра логов.
class LogsFactory(context: Context): ViewModelProvider.Factory {
    private val model by lazy{ PreferencesModel(context) }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LogsViewModel(model) as T
    }
}
