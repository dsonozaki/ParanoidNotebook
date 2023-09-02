package com.example.cmd.presentation.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.cmd.R
import com.example.cmd.databinding.AboutFragmentBinding

class AboutFragment: Fragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val aboutBinding =
      AboutFragmentBinding.inflate(inflater,container,false)
    (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.about)
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    aboutBinding.aboutmessage.movementMethod = LinkMovementMethod.getInstance()
    aboutBinding.aboutmessage.text = HtmlCompat.fromHtml(getString(R.string.about_long), HtmlCompat.FROM_HTML_MODE_LEGACY)
    requireActivity().addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
      }

      //Кнопка "назад"
      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) { findNavController().popBackStack() }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    return aboutBinding.root
  }

}
