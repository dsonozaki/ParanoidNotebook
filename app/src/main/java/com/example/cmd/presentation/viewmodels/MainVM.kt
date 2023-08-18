package com.example.cmd.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmd.R
import com.example.cmd.di.MainScreenNotificationChannel
import com.example.cmd.domain.entities.AppInitStatus
import com.example.cmd.domain.entities.ContainPasswords
import com.example.cmd.domain.entities.DeletionState
import com.example.cmd.domain.entities.Passwords
import com.example.cmd.domain.repositories.DeletionStatusRepository
import com.example.cmd.domain.repositories.StartScreenRepository
import com.example.cmd.presentation.UIText
import com.example.cmd.presentation.states.StartScreenState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

//viewmodel основного экрана
@HiltViewModel
class MainVM @AssistedInject constructor(
  private val startScreenRepository: StartScreenRepository,
  private val deletionStatusRepository: DeletionStatusRepository,
  private val _currentState: MutableStateFlow<StartScreenState>,
  @MainScreenNotificationChannel private val toastChannel: Channel<UIText>,
  @Assisted private val passwords: Passwords
) : ViewModel() {

  val currentState = _currentState.asStateFlow()

  val toastFlow = toastChannel.receiveAsFlow()

  init {
    viewModelScope.launch {
      _currentState.value = startScreenRepository.startScreenData.first().let {
        when (it.appInitStatus) {
          AppInitStatus.INITIALISING -> {
            if (passwords.isEmpty()) {
              StartScreenState.Initialize
            } else {
              startScreenRepository.showHint()
              StartScreenState.ShowHint()
            }
          }

          AppInitStatus.SHOW_HINT -> {
            StartScreenState.ShowHint()
          }

          AppInitStatus.INITIALISED -> {
            StartScreenState.NormalMode(UIText.UsualString(it.text))
          }
        }
      }
    }
  }

  fun onTextEntered(text: String) {
    viewModelScope.launch {
      when (passwords.containsString(text)) {
        ContainPasswords.CONTAINS_SETTINGS -> {
          if (currentState.value is StartScreenState.ShowHintEditing) {
            startScreenRepository.finishInitialisation()
          }
          _currentState.value = StartScreenState.SecretModeEditing(UIText.UsualString(text))
        }

        ContainPasswords.CONTAINS_NOTHING -> _currentState.value =
          StartScreenState.NormalModeEditing(UIText.UsualString(text))

        ContainPasswords.CONTAINS_MAIN -> {
          val deletionResultString =
            when (deletionStatusRepository.deletionStatus.first().deletionState) {
              DeletionState.COMPLETE -> R.string.too_late
              DeletionState.NOT_STARTED -> R.string.full_saved
              DeletionState.STARTED -> R.string.part_saved
            }
          deletionStatusRepository.preventDeletion()
          toastChannel.send(UIText.StringResource(deletionResultString))
          _currentState.value = StartScreenState.NormalModeEditing(UIText.UsualString(text))
        }
      }
    }

  }

  fun editText() {
    currentState.value.let {
      when (it) {
        is StartScreenState.NormalMode -> _currentState.value =
          StartScreenState.NormalModeEditing(it.text)

        is StartScreenState.ShowHint -> _currentState.value = StartScreenState.ShowHintEditing
        is StartScreenState.SecretMode -> _currentState.value =
          StartScreenState.SecretModeEditing(it.text)

        else -> throw RuntimeException("Strange StartScreenState, editText: ${it.javaClass.name}")
      }
    }
  }

  fun saveText() {
    viewModelScope.launch {
      currentState.value.let {
        when (it) {
          is StartScreenState.ShowHintEditing -> StartScreenState.ShowHint()
          is StartScreenState.NormalModeEditing -> {
            _currentState.value = StartScreenState.NormalMode(it.text)
            startScreenRepository.saveText(it.text.value.replace(passwords.mainPass,""))
          }
          is StartScreenState.SecretModeEditing -> {
            _currentState.value = StartScreenState.SecretMode(it.text)
            startScreenRepository.saveText(it.text.value.replace(passwords.settingsPass,""))
          }
          else -> throw RuntimeException("Strange StartScreenState, saveText: ${it.javaClass.name}")
        }
      }
    }
  }


}
