package com.example.cmd.presentation.states

import com.example.cmd.domain.entities.MyFileDomain


sealed class DeletionDataState {
  data object Loading : DeletionDataState()
  class ViewData(val items: List<MyFileDomain>) : DeletionDataState()
}
