package com.example.cmd.presentation.utils

import android.content.Context
import android.util.Log

sealed class UIText {
  data class UsualString(val value: String) : UIText()
  class StringResource(val id: Int, vararg val arguments: Any) : UIText()

  class ColoredHTMLText(val text: String, vararg val colors: Int) : UIText()

  private fun String.colorize(vararg colors: String): String  = buildString {
    this@colorize.lines().forEach {
     this@buildString.append(it.format(*colors))
    }
  }

  fun asString(context: Context?): String {
    return when (this) {
      is UsualString -> value
      is StringResource -> context?.getString(id, *arguments) ?: throw RuntimeException("context is not provided")
      is ColoredHTMLText -> {
        val colors = this.colors.map {
          context?.resources?.getString(it)?.removeRange(1..2) ?: throw RuntimeException("context is not provided")
        }.toTypedArray()
        Log.w("colors",colors.joinToString())
        Log.w("currenttext",this.text.colorize(*colors))
        return this.text.colorize(*colors)
      }
    }
  }
}
