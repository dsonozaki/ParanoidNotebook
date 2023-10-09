package com.example.cmd.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.cmd.R
import com.example.cmd.databinding.LogFragmentBinding
import com.example.cmd.formatDate
import com.example.cmd.launchLifecycleAwareCoroutine
import com.example.cmd.presentation.actions.LogsActions
import com.example.cmd.presentation.dialogs.InputDigitDialog
import com.example.cmd.presentation.dialogs.QuestionDialog
import com.example.cmd.presentation.utils.DateValidatorAllowed
import com.example.cmd.presentation.utils.DialogLauncher
import com.example.cmd.presentation.viewmodels.LogsVM
import com.example.cmd.presentation.viewmodels.LogsVM.Companion.CHANGE_TIMEOUT_REQUEST
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogsFragment : Fragment() {
  private val viewModel: LogsVM by viewModels()
  private var _logBinding: LogFragmentBinding? = null
  private val logBinding
    get() = _logBinding ?: throw RuntimeException("LogsFragmentBinding == null")
  private val controller by lazy { findNavController() }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _logBinding =
      LogFragmentBinding.inflate(inflater, container, false)
    logBinding.viewmodel = viewModel
    logBinding.lifecycleOwner = viewLifecycleOwner
    return logBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupActionBar()
    //Обработка результатов диалогов с пользователем
    setDialogsListeners()
    setupMenu()
    //Обработка событий
    setupActionsListener()
  }

  private fun setupActionsListener() {
    val dialogLauncher = DialogLauncher(parentFragmentManager, context)
    viewLifecycleOwner.launchLifecycleAwareCoroutine {
      viewModel.logsActionFlow.collect {
        when (it) {
          is LogsActions.ShowUsualDialog -> dialogLauncher.launchDialogFromAction(it.value)
          is LogsActions.showDatePicker -> with(it) { buildCalendar(dateValidator,selection) }
        }
      }
    }
  }

  private fun buildCalendar(dateValidatorAllowed: DateValidatorAllowed,selection: Long) {
    val constraintsBuilder =
      getConstraints(dateValidatorAllowed) //Настраиваем ограничения для date picker, разрешаем выбирать те дни, за которые доступны логи.
    val datePicker =
      makeDatePicker(constraintsBuilder, selection)
    datePicker.show(parentFragmentManager, MATERIAL_PICKER_TAG) //показываем календарь
    datePicker.addOnPositiveButtonClickListener {
      viewModel.openLogsForSelection(it)
    }
  }

  private fun makeDatePicker(constraintsBuilder: CalendarConstraints.Builder, selection: Long) =
    MaterialDatePicker.Builder.datePicker()
      .setTitleText(getString(R.string.select_date))
      .setSelection(
        selection
      )
      .setCalendarConstraints(constraintsBuilder.build())
      .setTheme(R.style.MyCalendar)
      .build()

  private fun getConstraints(dateValidatorAllowed: DateValidatorAllowed) = CalendarConstraints.Builder()
    .setValidator(
      dateValidatorAllowed
    )

  private fun setDialogsListeners() {
    QuestionDialog.setupListener(
      parentFragmentManager,
      CHANGE_TIMEOUT_REQUEST,
      viewLifecycleOwner
    ) {
      viewModel.clearLogsForDay()
    }
    InputDigitDialog.setupListener(parentFragmentManager, viewLifecycleOwner) {
      viewModel.changeAutoDeletionTimeout(it)
    }
  }

  private fun setupMenu() {
    requireActivity().addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.logs, menu)
      }


      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
          R.id.calendar -> {
            viewModel.buildCalendar() //обновляем логи в приложении
          }

          R.id.log_timeout -> viewModel.showChangeTimeoutDialog() //изменение тайм-аута очистки логов
          R.id.clear_logs -> viewModel.showClearLogsDialog() //очистка логов за день
          android.R.id.home -> controller.popBackStack()
          R.id.logs_help -> viewModel.showHelpDialog()

        }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
  }

  private fun setupActionBar() {
    viewLifecycleOwner.launchLifecycleAwareCoroutine {
      viewModel.logsState.collect {
        (activity as AppCompatActivity).supportActionBar?.title = it.date.formatDate()
      }
    }
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onDestroy() {
    _logBinding = null
    super.onDestroy()
  }

  companion object {
    private const val MATERIAL_PICKER_TAG = "material_picker_tag"
  }

}
