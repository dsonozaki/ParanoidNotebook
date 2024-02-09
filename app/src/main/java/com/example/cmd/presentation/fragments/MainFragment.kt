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
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.cmd.R
import com.example.cmd.databinding.MainFragmentBinding
import com.example.cmd.launchLifecycleAwareCoroutine
import com.example.cmd.presentation.MainActivity
import com.example.cmd.presentation.actions.StartScreenActions
import com.example.cmd.presentation.states.Page
import com.example.cmd.presentation.states.StartScreenState
import com.example.cmd.presentation.viewmodels.MainFragmentVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {
  private val viewModel: MainFragmentVM by viewModels()
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
    setupTextChangedListener()
    setupActionsListener()
    //снятие/восстановление маскировки
    getAccess()
    setupMainMenuListener()
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

  private fun setupMainMenuListener() {
    launchLifecycleAwareCoroutine {
      viewModel.currentState.collect {
        val mainActivity = activity as MainActivity
        when (it) {
          is StartScreenState.Loading -> {
          }

          is StartScreenState.ShowHintEditing -> {
            mainActivity.setPage(Page.MainFragmentNormal())
            setupToolbarMenu(true)
          }

          is StartScreenState.ShowHint, is StartScreenState.NormalMode -> {
            mainActivity.setPage(Page.MainFragmentNormal())
            setupToolbarMenu(false)
          }

          is StartScreenState.NormalModeEditing -> {
            mainActivity.setPage(Page.MainFragmentNormal())
            setupToolbarMenu(true)
          }

          is StartScreenState.SecretMode -> {
            mainActivity.setPage(Page.MainFragmentSecret())
            setupToolbarMenu(false)
          }

          is StartScreenState.SecretModeEditing -> {
            mainActivity.setPage(Page.MainFragmentSecret())
            setupToolbarMenu(true)
          }
        }
      }
    }
  }


  private fun setupToolbarMenu(editing: Boolean) {
    requireActivity().addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        if (editing) {
          menuInflater.inflate(R.menu.main_hidden_save, menu)
        } else {
          menuInflater.inflate(R.menu.main_hidden_edit, menu)
        }
      }

      //Открытие других экранов
      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
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

        is StartScreenActions.CreatePasswords -> (activity as MainActivity).controller.navigate(
          MainFragmentDirections.editFirstPassword()
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
