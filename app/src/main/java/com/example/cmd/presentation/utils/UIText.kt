package com.example.cmd.presentation.utils

import android.content.Context

sealed class UIText {
  data class UsualString(val value: String) : UIText()
  class StringResource(val id: Int, vararg val arguments: Any) : UIText()

  class ColoredHTMLText(val text: String, vararg val colors: Int) : UIText()

  private fun String.colorize(colors: List<String>): String  = buildString {
    this.lines().forEach {
     this.append(this@colorize.format(colors))
    }
  }

  fun asString(context: Context?): String {
    return when (this) {
      is UsualString -> value
      is StringResource -> context?.getString(id, *arguments) ?: throw RuntimeException("context is not provided")
      is ColoredHTMLText -> {
        val colors = this.colors.map {
          context?.resources?.getString(0+it) ?: throw RuntimeException("context is not provided")
        }
        return this.text.colorize(colors)
      }
    }
  }
}
