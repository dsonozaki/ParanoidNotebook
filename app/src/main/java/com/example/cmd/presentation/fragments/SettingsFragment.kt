package com.example.cmd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.cmd.presentation.MainActivity
import com.example.cmd.presentation.states.Page

class SettingsFragment: Fragment() {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setMainActivityState()
  }

  private fun setMainActivityState() {
    (activity as MainActivity).setPage(Page.Settings())
  }
}
