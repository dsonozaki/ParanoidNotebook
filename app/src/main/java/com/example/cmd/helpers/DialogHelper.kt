package com.example.cmd.helpers

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.cmd.R

//Класс для быстрого создания диалогов, заданных классами в dialog.
class DialogHelper(private val controller: NavController) {
  fun questionDialog(
    type: String, title: String,
    message: String,
  ) {
    val bundle = bundleOf("type" to type, "message" to message, "title" to title)
    controller.navigate(R.id.questionDialog, bundle)
  }

  fun infoDialog(
    title: String,
    message: String,
  ) {
    val bundle = bundleOf("message" to message, "title" to title)
    controller.navigate(R.id.infoDialog, bundle)
  }

  fun inputDialog(
    type: String, title: String,
    message: String, hint: String, dpi: Float
  ) {
    val bundle = bundleOf(
      "type" to type,
      "message" to message,
      "title" to title,
      "hint" to hint,
      "dpi" to dpi
    )
    controller.navigate(R.id.inputDialog, bundle)
  }
}
