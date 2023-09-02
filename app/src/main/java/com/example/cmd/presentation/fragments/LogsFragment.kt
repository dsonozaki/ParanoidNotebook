package com.example.cmd.presentation.fragments

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cmd.R
import com.example.cmd.databinding.LogFragmentBinding
import com.example.cmd.formatDate
import com.example.cmd.getMillis
import com.example.cmd.presentation.dialogs.InfoDialog
import com.example.cmd.presentation.dialogs.InputDigitDialog
import com.example.cmd.presentation.dialogs.QuestionDialog
import com.example.cmd.presentation.states.LogsScreenState
import com.example.cmd.presentation.utils.DateValidatorAllowed
import com.example.cmd.presentation.viewmodels.LogsVM
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
  }

  private fun setDialogsListeners() {
    QuestionDialog.setupListener(parentFragmentManager, CHANGE_TIMEOUT_REQUEST,viewLifecycleOwner) {
      viewModel.clearLogsForDay()
    }
    InputDigitDialog.setupListener(parentFragmentManager,viewLifecycleOwner) {
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
            buildCalendar() //обновляем логи в приложении
          }

          R.id.log_timeout -> {
            InputDigitDialog.show(
              parentFragmentManager,
              getString(R.string.enter_timeout_logs),
              viewModel.logsData.value.logsAutoRemovePeriod.toString(),
              "",
              0..10000
            )
          } //изменение тайм-аута очистки логов
          R.id.clear_logs -> {
            QuestionDialog.show(parentFragmentManager,getString(R.string.clear_logs_question),getString(R.string.logs_clear_warning,viewModel.logsState.value.date.formatDate()),CHANGE_TIMEOUT_REQUEST)
          } //очистка логов за день
          android.R.id.home -> controller.popBackStack()
          R.id.logs_help -> InfoDialog.show(parentFragmentManager,
            getString(R.string.help),
            getString(R.string.logs_help)
          )

        }
        return true
      }

      private fun buildCalendar() {
        val constraintsBuilder =
          getConstraints() //Настраиваем ограничения для date picker, разрешаем выбирать те дни, за которые доступны логи.
        val datePicker =
          makeDatePicker(constraintsBuilder)
        datePicker.show(parentFragmentManager, MATERIAL_PICKER_TAG) //показываем календарь
        datePicker.addOnPositiveButtonClickListener {
          viewModel.openLogsForSelection(it)
        }
      }

      private fun makeDatePicker(constraintsBuilder: CalendarConstraints.Builder) =
        MaterialDatePicker.Builder.datePicker()
          .setTitleText(getString(R.string.select_date))
          .setSelection(
            viewModel.logsState.value.date.getMillis()
          )
          .setCalendarConstraints(constraintsBuilder.build())
          .setTheme(R.style.MyCalendar)
          .build()

      private fun getConstraints() = CalendarConstraints.Builder()
        .setValidator(
          DateValidatorAllowed(
            viewModel.logsData.value.logDates.toSet()
          )
        )
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
  }

  private fun setupActionBar() {
    lifecycleScope.launch {
      viewModel.logsState.collect {
        Log.w("newState",(it is LogsScreenState.ViewLogs).toString())
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
    private const val CHANGE_TIMEOUT_REQUEST = "change_timeout_request"
  }

}
