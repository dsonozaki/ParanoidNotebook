package com.example.cmd.presentation.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.example.cmd.R
import com.example.cmd.databinding.AboutFragmentBinding
import com.example.cmd.presentation.MainActivity
import com.example.cmd.presentation.states.Page

class AboutFragment: Fragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val aboutBinding =
      AboutFragmentBinding.inflate(inflater,container,false)
    aboutBinding.aboutmessage.movementMethod = LinkMovementMethod.getInstance()
    aboutBinding.aboutmessage.text = HtmlCompat.fromHtml(getString(R.string.about_long), HtmlCompat.FROM_HTML_MODE_LEGACY)
    return aboutBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setMainActivityState()
  }

  private fun setMainActivityState() {
    (activity as MainActivity).setPage(Page.AboutParanoid())
  }

}
