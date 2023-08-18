package com.example.cmd.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class Passwords(val settingsPass: String ="",val mainPass:String="") {
  fun containsString(string: String): ContainPasswords {
    if (string.contains(settingsPass))
      return ContainPasswords.CONTAINS_SETTINGS
    if (string.contains(mainPass))
      return ContainPasswords.CONTAINS_MAIN
    return ContainPasswords.CONTAINS_NOTHING
  }

  fun isEmpty(): Boolean {
    return settingsPass.isEmpty() and mainPass.isEmpty()
  }
}

enum class ContainPasswords {
  CONTAINS_MAIN, CONTAINS_SETTINGS, CONTAINS_NOTHING
}
