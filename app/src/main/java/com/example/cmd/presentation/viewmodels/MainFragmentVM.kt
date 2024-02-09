package com.example.cmd.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.R
import com.example.cmd.domain.entities.AppInitStatus
import com.example.cmd.domain.entities.ContainPasswords
import com.example.cmd.domain.entities.DeletionStatus
import com.example.cmd.domain.entities.Passwords
import com.example.cmd.domain.usecases.autodeletion.status.GetDeletionStatusUseCase
import com.example.cmd.domain.usecases.autodeletion.status.PreventDeletionUseCase
import com.example.cmd.domain.usecases.logs.WriteToLogsUseCase
import com.example.cmd.domain.usecases.passwords.GetPasswordsFlowUseCase
import com.example.cmd.domain.usecases.startScreen.FinishInitializationUseCase
import com.example.cmd.domain.usecases.startScreen.GetDataUseCase
import com.example.cmd.domain.usecases.startScreen.SaveTextUseCase
import com.example.cmd.domain.usecases.startScreen.ShowHintUseCase
import com.example.cmd.presentation.actions.StartScreenActions
import com.example.cmd.presentation.states.StartScreenState
import com.example.cmd.presentation.utils.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//viewmodel основного экрана
@HiltViewModel
class MainFragmentVM @Inject constructor(
  private val _currentState: MutableStateFlow<StartScreenState>,
  getDataUseCase: GetDataUseCase,
  showHintUseCase: ShowHintUseCase,
  private val getDeletionStatusUseCase: GetDeletionStatusUseCase,
  private val preventDeletionUseCase: PreventDeletionUseCase,
  private val finishInitializationUseCase: FinishInitializationUseCase,
  private val saveTexUseCase: SaveTextUseCase,
  private val writeToLogsUseCase: WriteToLogsUseCase,
  private val getPasswordsFlowUseCase: GetPasswordsFlowUseCase,
  private val startScreenActionsChannel: Channel<StartScreenActions>,
) : ViewModel() {

  val currentState = _currentState.asStateFlow()

  val startScreenActionsFlow = startScreenActionsChannel.receiveAsFlow()

  private lateinit var passwords: Passwords


  init {
    viewModelScope.launch {
      getDataUseCase().let {
        passwords = getPasswordsFlowUseCase().first()
        when (it.appInitStatus) {
          AppInitStatus.INITIALISING -> {
            if (passwords.isEmpty()) {
              startScreenActionsChannel.send(StartScreenActions.CreatePasswords)
            } else {
              showHintUseCase()
              _currentState.emit(StartScreenState.ShowHint)
            }
          }

          AppInitStatus.SHOW_HINT -> {
            _currentState.emit(StartScreenState.ShowHint)
          }

          AppInitStatus.INITIALISED -> {
            _currentState.emit(StartScreenState.NormalMode(it.text))
          }
        }
      }
    }
  }

  fun onTextEntered(text: String) {
    viewModelScope.launch {
      when (passwords.containsString(text)) {
        ContainPasswords.CONTAINS_SETTINGS -> changeStateOnSettingsPassword()
        ContainPasswords.CONTAINS_NOTHING -> changeStateOnNoPassword()
        ContainPasswords.CONTAINS_MAIN -> showAutoDeletionStatus()
      }
    }
  }

  private suspend fun showAutoDeletionStatus() {
    val deletionResultString = when (val deletionStatus = getDeletionStatusUseCase().first()) {
      is DeletionStatus.Deleting -> R.string.part_saved
      is DeletionStatus.Completed ->
        if (deletionStatus.isActualState()) {
          R.string.too_late
        } else {
          R.string.full_saved
        }
      is DeletionStatus.Prevented -> if (deletionStatus.isActualState()) {
        R.string.new_prevention
      } else {
        R.string.full_saved
      }
    }
    preventDeletionUseCase()
    startScreenActionsChannel.send(
      StartScreenActions.ShowToast(
        UIText.StringResource(
          deletionResultString
        )
      )
    )
  }

  private suspend fun changeStateOnNoPassword() {
    if (currentState.value is StartScreenState.SecretModeEditing) {
      _currentState.emit(StartScreenState.NormalModeEditing)
    }
  }

  private suspend fun changeStateOnSettingsPassword() {
    Log.w("currentState",currentState.value.javaClass.toString())
    when (currentState.value) {
      is StartScreenState.ShowHintEditing -> {
        finishInitializationUseCase()
        _currentState.emit(StartScreenState.SecretModeEditing)
      }

      is StartScreenState.NormalModeEditing -> {
        startScreenActionsChannel.send(StartScreenActions.WriteToLogs)
        _currentState.emit(StartScreenState.SecretModeEditing)
      }

      else -> {}
    }
  }

  fun editText() {
    currentState.value.let {
      Log.w("page+editText",it.javaClass.name)
      when (it) {
        is StartScreenState.NormalMode -> _currentState.value =
          StartScreenState.NormalModeEditing

        is StartScreenState.ShowHint -> _currentState.value = StartScreenState.ShowHintEditing
        is StartScreenState.SecretMode -> _currentState.value =
          StartScreenState.SecretModeEditing

        else -> throw RuntimeException("Strange StartScreenState, editText: ${it.javaClass.name}")
      }
    }
  }

  fun saveText(text: String) {
    viewModelScope.launch {
      currentState.value.let {
        when (it) {
          is StartScreenState.NormalModeEditing -> {
            _currentState.value = StartScreenState.NormalMode()
            saveTexUseCase(text.replace(passwords.mainPass, ""))
          }

          is StartScreenState.SecretModeEditing -> {
            _currentState.value = StartScreenState.SecretMode
            saveTexUseCase(text.replace(passwords.settingsPass, ""))
          }

          else -> throw RuntimeException("Strange StartScreenState, saveText: ${it.javaClass.name}")
        }
      }
    }
  }

  fun writeLogs(string: String) {
    viewModelScope.launch {
      writeToLogsUseCase(string)
    }
  }


}
