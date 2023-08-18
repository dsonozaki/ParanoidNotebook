package com.example.cmd.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class StartScreenData(
  val appInitStatus: AppInitStatus = AppInitStatus.INITIALISING,
  val text: String = "")
