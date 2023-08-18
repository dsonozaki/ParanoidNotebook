package com.example.cmd.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmd.model.PreferencesModel
import com.example.cmd.presentation.viewmodels.LogsVM
//factory для viewmodel экрана просмотра логов.
class LogsFactory(context: Context): ViewModelProvider.Factory {
    private val model by lazy{ PreferencesModel(context) }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LogsVM(model) as T
    }
}
