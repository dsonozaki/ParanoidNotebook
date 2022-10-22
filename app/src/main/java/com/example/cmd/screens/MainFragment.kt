package com.example.cmd.screens

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.cmd.R
import com.example.cmd.databinding.MainFragmentBinding
import com.example.cmd.factory.MainFactory
import com.example.cmd.helpers.Request
import com.example.cmd.viewmodel.MainViewModel


class MainFragment : Fragment() {
  private val viewModel by viewModels<MainViewModel> { MainFactory(requireContext()) }
  private val controller by lazy { findNavController() }
  private val version by lazy { android.os.Build.VERSION.SDK_INT.toLong() }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val mainBinding =
      MainFragmentBinding.inflate(inflater, container, false)
    (activity as AppCompatActivity).supportActionBar?.title =
      resources.getString(R.string.app_name) //маскировочный заголовок
    mainBinding.viewmodel = viewModel
    mainBinding.lifecycleOwner = this
    mainBinding.notebook.addTextChangedListener {
      viewModel.changed()
    } //реакция на изменение текста
    val requestPermissionLauncher =
      registerForActivityResult(
        ActivityResultContracts.RequestPermission()
      ) { granted ->
        if (granted)
          viewModel.accessGranted()
        else
          Toast.makeText(
            requireContext(),
            getString(R.string.access_denied),
            Toast.LENGTH_SHORT
          ).show()
      }
    viewModel.action.observe(viewLifecycleOwner) {
      when (it.get()?.type) {
        Request.Type.TOAST -> {
          Toast.makeText(requireContext(), it.list[0], Toast.LENGTH_SHORT).show()
        }
        Request.Type.MOVE -> {
          controller.navigate(R.id.editPasswords)
        }
        //Получение доступа к файлам
        Request.Type.ACCESS -> {
          if (version < 30)
            requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
          else
            viewModel.accessGranted()
        }
        else -> {}
      }
    }

    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    //снятие/восстановление маскировки
    requireActivity().addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.settings, menu)
        viewModel.menu.observe(activity as AppCompatActivity) {
          with(menu) {
            findItem(R.id.passwords)?.setEnabled(it)?.isVisible = it
            findItem(R.id.deletion)?.setEnabled(it)?.isVisible = it
            findItem(R.id.aboutApp)?.setEnabled(it)?.isVisible = it
            findItem(R.id.log)?.setEnabled(it)?.isVisible = it
          }
          if (it) {
            (activity as AppCompatActivity).supportActionBar?.title =
              resources.getString(R.string.true_app_name)
          } else {
            (activity as AppCompatActivity).supportActionBar?.title =
              resources.getString(R.string.app_name)
          }
        }
      }

      //Открытие других экранов
      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val id = when (menuItem.itemId) {
          R.id.passwords -> R.id.editPasswordsFragment

          R.id.deletion -> R.id.toDeletionSettings

          R.id.aboutApp -> R.id.showAppInfo
          R.id.log -> R.id.openLogs
          else -> null
        }
        controller.navigate(id!!)
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    return mainBinding.root
  }


  //обновление паролей
  override fun onResume() {
    viewModel.passwords()
    super.onResume()
  }

  //сохранение текста в блокноте
  override fun onStop() {
    viewModel.save()
    super.onStop()
  }
}
