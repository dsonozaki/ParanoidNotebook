package com.example.cmd.presentation.states

import com.example.cmd.R

sealed class Page(open val title: Int=0, open val hidden: Boolean) {
  class MainFragmentNormal(override val title: Int= R.string.app_name, override val hidden:Boolean=false): Page(title, hidden)
  class MainFragmentSecret(override val title: Int = R.string.true_app_name, override val hidden: Boolean=true): Page(title, hidden)
  class ChangePasswords(override val title: Int= R.string.change_passwords, override val hidden: Boolean=true): Page(title, hidden)
  class SetupDeletion(override val title: Int= R.string.deletion_settings, override val hidden: Boolean=true): Page(title, hidden)
  class Logs(val day: String, override val hidden: Boolean=true): Page(hidden=hidden)
  class AboutParanoid(override val title: Int = R.string.about, override val hidden: Boolean=true): Page(title, hidden)
  class Profile(override val title: Int = R.string.profile, override val hidden: Boolean=false): Page(title, hidden)
  class Settings(override val title: Int = R.string.fake_settings, override val hidden: Boolean=false): Page(title, hidden)
  class About(override val title: Int = R.string.aboutprog, override val hidden: Boolean=false): Page(title, hidden)
}
