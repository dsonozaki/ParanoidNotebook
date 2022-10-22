package com.example.cmd.helpers

import android.content.Context

//Класс для получения строки из ресурса в ViewModel
class StringResource(private val context: Context) {
  fun getString(id: Int, vararg format: Any) = context.getString(id, *format)

}
