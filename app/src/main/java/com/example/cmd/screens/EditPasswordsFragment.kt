package com.example.cmd.screens

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.cmd.R
import com.example.cmd.databinding.EditPasswordsBinding
import com.example.cmd.factory.EditPasswordsFactory
import com.example.cmd.viewmodel.EditPasswordsViewModel

class EditPasswordsFragment : Fragment() {
  private val viewModel by viewModels<EditPasswordsViewModel> {
    EditPasswordsFactory(
      requireContext()
    )
  }
  private val controller by lazy { findNavController() }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    (activity as AppCompatActivity).supportActionBar?.hide()
    val passwordsBinding = EditPasswordsBinding.inflate(inflater, container, false)
    passwordsBinding.viewmodel = viewModel
    passwordsBinding.lifecycleOwner = this

    //Смена цвета кнопки
    viewModel.color.observe(requireActivity()) {
      if (it) {
        setColor(passwordsBinding, R.color.amtheme)
      } else {
        setColor(passwordsBinding, R.color.lightgrey)
      }
    }
    viewModel.back.observe(requireActivity()) {
      if (it) {
        controller.popBackStack()
      } else {
        controller.navigate(R.id.passwordsInitialized)
      }
    }

    viewModel.toastMessage.observe(requireActivity()) { request ->
      request.get()?.let {
        Toast.makeText(
          context,
          getString(R.string.passwords_intersection, it.list[0], it.list[1]),
          Toast.LENGTH_SHORT
        ).show()
      }
    }
    return passwordsBinding.root
  }

  private fun setColor(passwordsBinding: EditPasswordsBinding, color: Int) {
    passwordsBinding.nextButton.strokeColor = ColorStateList.valueOf(
      ResourcesCompat.getColor(
        resources,
        color, requireActivity().theme
      )
    )
    passwordsBinding.nextButton.setTextColor(
      ColorStateList.valueOf(
        ResourcesCompat.getColor(
          resources,
          color, requireActivity().theme
        )
      )
    )
  }

  override fun onPause() {
    (activity as AppCompatActivity).supportActionBar?.show()
    super.onPause()
  }
}
