package com.example.cmd.presentation

import android.content.Context

sealed class UIText {
  data class UsualString(val value: String): UIText()
  class StringResource(val id: Int, vararg val arguments: Any): UIText()

  fun asString(context: Context){
    when (this) {
      is UsualString -> value
      is StringResource -> context.getString(id,*arguments)
    }
  }
}
