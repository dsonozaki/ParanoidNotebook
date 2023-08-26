package com.example.cmd.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.domain.entities.AutoDeletionData
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.domain.entities.MyFileDomain
import com.example.cmd.domain.usecases.autodeletion.data.GetAutoDeletionDataUseCase
import com.example.cmd.domain.usecases.autodeletion.data.PutAutoDeletionTimeOutUseCase
import com.example.cmd.domain.usecases.autodeletion.data.SwitchAutoDeletionStatusUseCase
import com.example.cmd.domain.usecases.autodeletion.data.XiomiNotificationSentUseCase
import com.example.cmd.domain.usecases.database.ChangeFilePriorityUseCase
import com.example.cmd.domain.usecases.database.ChangeSortOrderUseCase
import com.example.cmd.domain.usecases.database.ClearDbUseCase
import com.example.cmd.domain.usecases.database.DeleteMyFileUseCase
import com.example.cmd.domain.usecases.database.GetFilesDbUseCase
import com.example.cmd.domain.usecases.database.InsertMyFileUseCase
import com.example.cmd.presentation.states.DeletionSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//viewmodel для настроек файлов для удаления
@HiltViewModel
class DeletionSettingsVM @Inject constructor(
  getFilesDbUseCase: GetFilesDbUseCase,
  getAutoDeletionDataUseCase: GetAutoDeletionDataUseCase,
  private val deleteMyFileUseCase: DeleteMyFileUseCase,
  private val xiomiNotificationSentUseCase: XiomiNotificationSentUseCase,
  private val switchAutoDeletionStatusUseCase: SwitchAutoDeletionStatusUseCase,
  private val insertMyFileUseCase: InsertMyFileUseCase,
  private val changeFilePriorityUseCase: ChangeFilePriorityUseCase,
  private val putAutoDeletionTimeOutUseCase: PutAutoDeletionTimeOutUseCase,
  private val changeSortOrderUseCase: ChangeSortOrderUseCase,
  private val clearDbUseCase: ClearDbUseCase,
) : ViewModel() {

  val deletionSettingsState = getFilesDbUseCase().combineTransform(getAutoDeletionDataUseCase()) {
      files: List<MyFileDomain>, autoDeletionData: AutoDeletionData -> emit(DeletionSettingsState.ViewData(autoDeletionData,files))
  }.stateIn(viewModelScope,
    SharingStarted.Lazily,
    DeletionSettingsState.Loading
  )

  fun removeFileFromDb(uri: Uri) {
    viewModelScope.launch {
      deleteMyFileUseCase(uri)
    }
  }

  fun xiaomiNotificationSent() {
    viewModelScope.launch {
      xiomiNotificationSentUseCase()
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
