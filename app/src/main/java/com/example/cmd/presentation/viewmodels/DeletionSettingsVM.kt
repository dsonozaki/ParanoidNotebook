package com.example.cmd.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.R
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.domain.entities.MyFileDomain
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
import com.example.cmd.presentation.actions.DeletionSettingsAction
import com.example.cmd.presentation.actions.DialogActions
import com.example.cmd.presentation.states.DeletionActivationStatus
import com.example.cmd.presentation.states.DeletionDataState
import com.example.cmd.presentation.states.DeletionSettingsState
import com.example.cmd.presentation.utils.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
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
  private val deletionSettingsActionChannel: Channel<DeletionSettingsAction>
) : ViewModel() {

  val deletionSettingsActionFlow = deletionSettingsActionChannel.receiveAsFlow()

  val autoDeletionSettingsState = getAutoDeletionDataUseCase().map {
    DeletionSettingsState.ViewSettings(
      it.timeOut,
      it.toDeletionActivationStatus()
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    DeletionSettingsState.Loading
  )

  val autoDeletionDataState =
    getFilesDbUseCase().map { DeletionDataState.ViewData(it) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DeletionDataState.Loading
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

  fun showHelp() {
    viewModelScope.launch {
      deletionSettingsActionChannel.send(
        DeletionSettingsAction.ShowUsualDialog(
          DialogActions.ShowInfoDialog(
            UIText.StringResource(
              R.string.help
            ),
            UIText.StringResource(R.string.long_help)
          )
        )
      )
    }
  }

  fun showFileInfo(file: MyFileDomain) {
    viewModelScope.launch {
      with(file) {
        deletionSettingsActionChannel.send(
          DeletionSettingsAction.ShowUsualDialog(
            DialogActions.ShowInfoDialog(
              UIText.StringResource(
                R.string.about_file_title
              ), UIText.StringResource(R.string.fileDetails, name, priority, sizeFormatted)
            )
          )
        )
      }
    }
  }

  fun showPriorityEditor(file: MyFileDomain) {
    viewModelScope.launch {
      deletionSettingsActionChannel.send(
        DeletionSettingsAction.ShowPriorityEditor(
          UIText.StringResource(R.string.changePriority),
          file.priority.toString(),
          UIText.StringResource(R.string.file, file.name),
          file.uri.toString(),
          0..10000
        )
      )
    }
  }

  private suspend fun showXiaomiPermissionDialog() {
    deletionSettingsActionChannel.send(
      DeletionSettingsAction.ShowUsualDialog(
        DialogActions.ShowQuestionDialog(
          UIText.StringResource(
            R.string.are_you_sure_autodelete
          ), UIText.StringResource(R.string.stupid_chinese_phone), XIAOMI_NOTIFICATION_REQUEST
        )
      )
    )
  }


  private suspend fun showConfirmAutodeletionDialog(timeOut: Int) {
    deletionSettingsActionChannel.send(
      DeletionSettingsAction.ShowUsualDialog(
        DialogActions.ShowQuestionDialog(
          UIText.StringResource(
            R.string.are_you_sure_autodelete
          ),
          UIText.StringResource(R.string.final_explanation, timeOut),
          CONFIRM_AUTODELETION_REQUEST
        )
      )
    )
  }


  fun changeAutoDeletionStatus() {
    viewModelScope.launch {
      val deletionSettings = autoDeletionSettingsState.first()
      if (deletionSettings is DeletionSettingsState.ViewSettings) {
        when (deletionSettings.status) {
          DeletionActivationStatus.ACTIVE -> switchAutoDeletionStatusUseCase()
          DeletionActivationStatus.INACTIVE_AND_WITHOUT_TIMEOUT -> showAutoDeletionTimeoutNotReadyDialog()
          DeletionActivationStatus.INACTIVE_AND_NOT_NOTIFIED_XIAOMI -> showXiaomiPermissionDialog()
          DeletionActivationStatus.INACTIVE_AND_READY -> showConfirmAutodeletionDialog(
            deletionSettings.timeout
          )
        }
      }
    }
  }

  fun showInputTimeoutDialog() {
    viewModelScope.launch {
      val deletionSettings = autoDeletionSettingsState.first()
      if (deletionSettings is DeletionSettingsState.ViewSettings) {
        showAutoDeletionTimeoutDialog(deletionSettings.timeout)
      }
    }
  }

  private suspend fun showAutoDeletionTimeoutDialog(timeout: Int) {
    deletionSettingsActionChannel.send(
      DeletionSettingsAction.ShowUsualDialog(
        DialogActions.ShowInputDialog(
          UIText.StringResource(R.string.timeout_please),
          timeout.toString(),
          UIText.StringResource(R.string.timeout_long),
          1..1000
        )
      )
    )
  }

  fun showClearDialog() {
    viewModelScope.launch {
      deletionSettingsActionChannel.send(
        DeletionSettingsAction.ShowUsualDialog(
          DialogActions.ShowQuestionDialog(
            UIText.StringResource(R.string.apply),
            UIText.StringResource(R.string.want_to_clear),
            CONFIRM_CLEAR_REQUEST
          )
        )
      )
    }
  }

  private suspend fun showAutoDeletionTimeoutNotReadyDialog() {
    deletionSettingsActionChannel.send(
      DeletionSettingsAction.ShowUsualDialog(
        DialogActions.ShowQuestionDialog(
          UIText.StringResource(
            R.string.please_set_timeout1
          ), UIText.StringResource(R.string.please_set_timeout2), TIMEOUT_NOT_READY_REQUEST
        )
      )
    )
  }


  fun addFileToDb(uri: Uri, isDirectory: Boolean) {
    viewModelScope.launch {
      insertMyFileUseCase(uri, isDirectory)
    }
  }

  fun changeFilePriority(priority: Int, uri: Uri) {
    viewModelScope.launch {
      changeFilePriorityUseCase(priority, uri)
    }
  }

  fun changeAutodeletionTimeout(timeout: Int) {
    viewModelScope.launch {
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

  companion object {
    const val TIMEOUT_NOT_READY_REQUEST = "set_timeout_request"
    const val CONFIRM_AUTODELETION_REQUEST = "confirm_autodeletion_start"
    const val CONFIRM_CLEAR_REQUEST = "confirm_clear_request"
    const val XIAOMI_NOTIFICATION_REQUEST = "show_xiaomi_notification"
  }
}
