package com.example.cmd.presentation.fragments

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cmd.R
import com.example.cmd.databinding.MainFragmentBinding
import com.example.cmd.launchLifecycleAwareCoroutine
import com.example.cmd.presentation.actions.StartScreenActions
import com.example.cmd.presentation.states.StartScreenState
import com.example.cmd.presentation.viewmodels.MainVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {
  private val viewModel: MainVM by viewModels()
  private val controller by lazy { findNavController() }
  private var _binding: MainFragmentBinding? = null
  private val binding
    get() = _binding ?: throw RuntimeException("MainFragmentBinding == null")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding =
      MainFragmentBinding.inflate(inflater, container, false)
    binding.viewmodel = viewModel
    binding.lifecycleOwner = viewLifecycleOwner
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (activity as AppCompatActivity).supportActionBar?.title =
      resources.getString(R.string.app_name) //маскировочный заголовок
    setupTextChangedListener()
    setupActionsListener()
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    //снятие/восстановление маскировки
    getAccess()
    setupMenu()
  }

  private fun getAccess() {
    val requestPermissionLauncher =
      registerForActivityResult(
        ActivityResultContracts.RequestPermission()
      ) { granted ->
        if (!granted)
          Toast.makeText(
            requireContext(),
            getString(R.string.access_denied),
            Toast.LENGTH_SHORT
          ).show()
      }
    if (ContextCompat.checkSelfPermission(requireActivity(), WRITE_EXTERNAL_STORAGE)
      != PackageManager.PERMISSION_GRANTED
    ) {
      requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
    }
  }

  private fun setupMenu() {
    requireActivity().addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_uncovered_save, menu)
        lifecycleScope.launch {
          repeatOnLifecycle(Lifecycle.State.RESUMED) {
            menuItemsFromState(menu)
          }
        }
      }

      private suspend fun menuItemsFromState(menu: Menu) {
        viewModel.currentState.collect {
          when (it) {
            is StartScreenState.Loading -> {}
            is StartScreenState.ShowHintEditing -> {
              menu.changeItems(setOf())
            }

            is StartScreenState.ShowHint, is StartScreenState.NormalMode -> {
              (activity as AppCompatActivity).supportActionBar?.title =
                resources.getString(R.string.app_name)
              menu.changeItems(setOf(R.id.edit_text))
            }

            is StartScreenState.NormalModeEditing -> {
              (activity as AppCompatActivity).supportActionBar?.title =
                resources.getString(R.string.app_name)
              menu.changeItems(setOf(R.id.save))
            }

            is StartScreenState.SecretMode -> {
              (activity as AppCompatActivity).supportActionBar?.title =
                resources.getString(R.string.true_app_name)
              menu.changeItems(
                setOf(
                  R.id.edit_text,
                  R.id.passwords,
                  R.id.deletion,
                  R.id.log,
                  R.id.aboutApp
                )
              )
            }

            is StartScreenState.SecretModeEditing -> {
              (activity as AppCompatActivity).supportActionBar?.title =
                resources.getString(R.string.true_app_name)
              menu.changeItems(
                setOf(
                  R.id.save,
                  R.id.passwords,
                  R.id.deletion,
                  R.id.log,
                  R.id.aboutApp
                )
              )

            }
          }
        }
      }

      private fun MenuItem.switchItemVisibility(visibility: Boolean) {
        isEnabled = visibility
        isVisible = visibility
      }

      private fun Menu.changeItems(items: Set<Int>) {
        for (i in 0..<size()) {
          val item = getItem(i)
          if (item.itemId in items) {
            item.switchItemVisibility(true)
          } else {
            item.switchItemVisibility(false)
          }
        }
      }


      //Открытие других экранов
      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
          R.id.passwords -> controller.navigate(MainFragmentDirections.editPasswords())
          R.id.deletion -> controller.navigate(MainFragmentDirections.toDeletionSettings())
          R.id.aboutApp -> controller.navigate(MainFragmentDirections.showAppInfo())
          R.id.log -> controller.navigate(MainFragmentDirections.openLogs())
          R.id.edit_text -> viewModel.editText()
          R.id.save -> viewModel.saveText(binding.notebook.text.toString())
        }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
  }

  private fun setupActionsListener() {
    viewLifecycleOwner.launchLifecycleAwareCoroutine {
      actionFromState()
    }
  }

  private suspend fun actionFromState() {
    viewModel.startScreenActionsFlow.collect {
      when (it) {
        is StartScreenActions.ShowToast -> Toast.makeText(
          requireContext(),
          it.message.asString(context),
          Toast.LENGTH_SHORT
        ).show()

        is StartScreenActions.CreatePasswords -> controller.navigate(
          MainFragmentDirections.createPasswords(
            true
          )
        )

        is StartScreenActions.WriteToLogs -> viewModel.writeLogs(getString(R.string.enter))
      }
    }
  }

  private fun setupTextChangedListener() {
    binding.notebook.addTextChangedListener {
      viewModel.onTextEntered(it.toString())
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
