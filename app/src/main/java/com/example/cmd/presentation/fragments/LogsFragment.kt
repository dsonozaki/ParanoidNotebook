package com.example.cmd.presentation.fragments

import android.os.Bundle
import android.view.*
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.cmd.R
import com.example.cmd.databinding.LogBinding
import com.example.cmd.presentation.factory.LogsFactory
import com.example.cmd.helpers.DateValidatorAllowed
import com.example.cmd.helpers.DialogHelper
import com.example.cmd.presentation.viewmodels.LogsVM
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker

import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.*

class LogsFragment: Fragment() {
  private val dpi by lazy { resources.displayMetrics.density }
  private val viewModel by viewModels<LogsVM> {
    LogsFactory(requireContext())
  }
  private lateinit var logBinding: LogBinding
  private val controller by lazy { findNavController() }
  private val dialogHelper by lazy { DialogHelper(controller) }
  private val sdf by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    logBinding =
      LogBinding.inflate(inflater,container,false)
    logBinding.viewmodel = viewModel
    (activity as AppCompatActivity).supportActionBar?.title = viewModel.day
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    logBinding.lifecycleOwner = this
    viewModel.scroll.observe(viewLifecycleOwner) {
      scrollDown()
    }
    //Обработка результатов диалогов с пользователем
    setFragmentResultListener(
      "request"
    ) { _, result ->
      when (result.getString("response")) {
        "change_logs_timeout" ->
          try {
            val days = result.getString("extra")!!.toLong()
            viewModel.logTimeout(days)
            controller.popBackStack()
          } catch (e: Exception) {
            Toast.makeText(context, "Пожалуйста, введите целое число", Toast.LENGTH_SHORT)
              .show()
          }
        "clearLogs" -> {
          viewModel.clearLogs()
          viewModel.loadLogs()
        }
      }
    }
    requireActivity().addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.logs, menu)
      }


      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
          R.id.calendar -> {
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            val constraintsBuilder =
              CalendarConstraints.Builder()
                .setValidator(
                  DateValidatorAllowed(
                    viewModel.getAllowed() + sdf.format(
                      Date(
                        today
                      )
                    )
                  )
                ) //Настраиваем ограничения для date picker, разрешаем выбирать те дни, за которые доступны логи.
            val datePicker =
              MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_date))
                .setSelection(
                  LocalDate.parse(viewModel.day).toEpochDays().toLong() * 86400000
                )
                .setCalendarConstraints(constraintsBuilder.build())
                .setTheme(R.style.MyCalendar)
                .build()
            datePicker.show(parentFragmentManager, "2") //показываем календарь
            datePicker.addOnPositiveButtonClickListener {
              val day = sdf.format(Date(it))
              viewModel.day = day
              (activity as AppCompatActivity).supportActionBar?.title = day
              logBinding.data.text = ""
              viewModel.loadLogs()
            } //обновляем логи в приложении
          }
          R.id.log_timeout -> {
            dialogHelper.inputDialog(
              "change_logs_timeout",
              "Введите тайм-аут очистки логов в днях",
              "",
              viewModel.getTimeLag().toString(),
              dpi
            )
          } //изменение тайм-аута очистки логов
          R.id.clear_logs -> {
            dialogHelper.questionDialog(
              "clearLogs",
              "Очистить логи?",
              "Логи за ${viewModel.day} будут стёрты"
            )
          } //очистка логов за день
          android.R.id.home -> controller.popBackStack()
          R.id.logs_help -> dialogHelper.infoDialog(
            getString(R.string.help),
            getString(R.string.logs_help)
          )
          R.id.refresh -> viewModel.loadLogs()
        }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    return logBinding.root
  }

  private fun scrollDown() {
    logBinding.scrollView3.post {
      logBinding.scrollView3.fullScroll(ScrollView.FOCUS_DOWN)
    }
  }
}
