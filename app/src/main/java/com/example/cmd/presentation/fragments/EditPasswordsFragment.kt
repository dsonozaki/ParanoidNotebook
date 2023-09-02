package com.example.cmd.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cmd.databinding.EditPasswordsFragmentBinding
import com.example.cmd.presentation.viewmodels.EditPasswordsVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPasswordsFragment : Fragment() {
  private val viewModel: EditPasswordsVM by viewModels()
  private val controller by lazy { findNavController() }
  private var _editPasswordsBinding: EditPasswordsFragmentBinding? = null

  private val editPasswordsBinding
    get() = _editPasswordsBinding ?: throw RuntimeException("EditPasswordsFragmentBinding == null")

  private val args by navArgs<EditPasswordsFragmentArgs>()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _editPasswordsBinding = EditPasswordsFragmentBinding.inflate(inflater, container, false)
    editPasswordsBinding.viewmodel = viewModel
    editPasswordsBinding.lifecycleOwner = viewLifecycleOwner
    return editPasswordsBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (activity as AppCompatActivity).supportActionBar?.hide()
    setupButtons()
    setupTextChangedListener()
    setupGoToMainScreenListener()
  }

  private fun setupGoToMainScreenListener() {
    lifecycleScope.launch {
      viewModel.goToMainScreenFlow.collect {
        controller.navigate(EditPasswordsFragmentDirections.passwordsInitialized())
      }
    }
  }

  private fun setupTextChangedListener() {
    editPasswordsBinding.settingsPasswordInput.addTextChangedListener {
      viewModel.passwordsChanged(
        editPasswordsBinding.mainPasswordInput.text.toString(),
        it.toString()
      )
    }
    editPasswordsBinding.mainPasswordInput.addTextChangedListener {
      viewModel.passwordsChanged(
        it.toString(),
        editPasswordsBinding.settingsPasswordInput.text.toString()
      )
    }
  }

  private fun setupButtons() {
    if (args.firstPassword) {
      editPasswordsBinding.backButton.visibility = View.GONE
    } else {
      editPasswordsBinding.backButton.setOnClickListener {
          controller.popBackStack()
        }
    }
    editPasswordsBinding.nextButton.setOnClickListener {
      viewModel.updatePasswords(
        editPasswordsBinding.mainPasswordInput.text.toString(),
        editPasswordsBinding.settingsPasswordInput.text.toString()
      )
    }
  }



  override fun onDestroyView() {
    super.onDestroyView()
    (activity as AppCompatActivity).supportActionBar?.show()
    _editPasswordsBinding = null
  }

}
