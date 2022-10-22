package com.example.cmd.helpers

//Класс для передачи событий от ViewModel к View.
class Request(val type: Type, val list: List<String> = listOf()) {
  enum class Type {
    TIMEOUT, QUESTION, PRIORITY, TOAST, ALERT, MOVE, ACCESS
  }

  var hasBeenHandled = false
    private set

  fun get(): Request? {
    return if (hasBeenHandled) {
      null
    } else {
      hasBeenHandled = true
      this
    }
  }

}
