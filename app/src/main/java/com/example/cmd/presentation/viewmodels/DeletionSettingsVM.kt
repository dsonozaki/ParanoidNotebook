package com.example.cmd.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.domain.usecases.autodeletion.data.GetAutoDeletionDataUseCase
import com.example.cmd.domain.usecases.autodeletion.data.PutAutoDeletionTimeOutUseCase
import com.example.cmd.domain.usecases.autodeletion.data.SwitchAutoDeletionStatusUseCase
import com.example.cmd.domain.usecases.autodeletion.data.XiaomiNotificationSentUseCase
import com.example.cmd.domain.usecases.database.ChangeFilePriorityUseCase
import com.example.cmd.domain.usecases.database.ChangeSortOrderUseCase
import com.example.cmd.domain.usecases.database.ClearDbUseCase
import com.example.cmd.domain.usecases.database.DeleteMyFileUseCase
import com.example.cmd.domain.usecases.database.GetFilesDbUseCase
import com.example.cmd.domain.usecases.database.InsertMyFileUseCase
import com.example.cmd.presentation.states.DeletionSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//viewmodel для настроек файлов для удаления
@HiltViewModel
class DeletionSettingsVM @Inject constructor(
  getFilesDbUseCase: GetFilesDbUseCase,
  getAutoDeletionDataUseCase: GetAutoDeletionDataUseCase,
  private val deleteMyFileUseCase: DeleteMyFileUseCase,
  private val xiaomiNotificationSentUseCase: XiaomiNotificationSentUseCase,
  private val switchAutoDeletionStatusUseCase: SwitchAutoDeletionStatusUseCase,
  private val insertMyFileUseCase: InsertMyFileUseCase,
  private val changeFilePriorityUseCase: ChangeFilePriorityUseCase,
  private val putAutoDeletionTimeOutUseCase: PutAutoDeletionTimeOutUseCase,
  private val changeSortOrderUseCase: ChangeSortOrderUseCase,
  private val clearDbUseCase: ClearDbUseCase,
) : ViewModel() {

  val autoDeletionDataState = getAutoDeletionDataUseCase().map {
    DeletionSettingsState.ViewData(it.timeOut,it.toDeletionActivationStatus())
  }.stateIn(viewModelScope,
    SharingStarted.Lazily,
    DeletionSettingsState.Loading
  )

  val filesState = getFilesDbUseCase().stateIn(
    viewModelScope,
    SharingStarted.Lazily,
    listOf()
  )

  fun removeFileFromDb(uri: Uri) {
    viewModelScope.launch {
      deleteMyFileUseCase(uri)
    }
  }

  fun xiaomiNotificationSent() {
    viewModelScope.launch {
      xiaomiNotificationSentUseCase()
    }
  }

  fun switchAutoDeletionStatus() {
    viewModelScope.launch {
      switchAutoDeletionStatusUseCase()
    }
  }

  fun addFileToDb(uri: Uri, isDirectory: Boolean) {
    viewModelScope.launch {
      insertMyFileUseCase(uri, isDirectory)
    }
  }

  fun changeFilePriority(priority: Int, uri: Uri) {
    viewModelScope.launch {
      changeFilePriorityUseCase(priority,uri)
    }
  }

  fun changeAutodeletionTimeout(timeout: Int) {
    viewModelScope.launch{
      putAutoDeletionTimeOutUseCase(timeout)
    }
  }


  fun clearFilesDb() {
    viewModelScope.launch {
      clearDbUseCase()
    }
  }

  fun changeSortOrder(sortOrder: FilesSortOrder) {
    viewModelScope.launch {
      changeSortOrderUseCase(sortOrder)
    }
  }
}
