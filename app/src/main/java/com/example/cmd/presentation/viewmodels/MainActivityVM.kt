package com.example.cmd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.presentation.states.Page
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityVM @Inject constructor(): ViewModel () {

  private val _currentPage = MutableStateFlow<Page>(Page.MainFragmentNormal())
  val currentPage = _currentPage.asStateFlow()
  fun setPage(page: Page) {
    viewModelScope.launch {
      _currentPage.emit(page)
    }
  }
}
