package com.example.cmd.presentation

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.cmd.R
import com.example.cmd.databinding.ActivityMainBinding
import com.example.cmd.launchLifecycleAwareCoroutine
import com.example.cmd.presentation.states.Page
import com.example.cmd.presentation.viewmodels.MainActivityVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  private val viewModel: MainActivityVM by viewModels()
  private lateinit var mainBinding: ActivityMainBinding
  lateinit var controller: NavController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mainBinding =
      ActivityMainBinding.inflate(layoutInflater)
    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.navHostFragment)
        as NavHostFragment
    controller = navHostFragment.navController
    setSupportActionBar(mainBinding.toolbar2)
    setContentView(mainBinding.root)
    observePage()
  }

  private fun observePage() {
    this.launchLifecycleAwareCoroutine {
      viewModel.currentPage.collect {
        Log.w("page",it.javaClass.name)
        setNavigation(it.hidden)
        when(it) {
          is Page.Logs -> {
            title = it.day
          }
          else -> {
            setTitle(it.title)
          }
        }
      }
    }
  }

  fun setPage(page: Page) {
    viewModel.setPage(page)
  }

  fun changeEditIcon(edit: Boolean) {
    mainBinding.toolbar2.menu.clear()
    if (edit) {
      mainBinding.toolbar2.inflateMenu(R.menu.main_hidden_edit)
    } else {
      mainBinding.toolbar2.inflateMenu(R.menu.main_hidden_save)
    }
  }

  private fun setNavigation(hidden: Boolean) {
    val drawer = mainBinding.drawer
    mainBinding.navigationView.menu.clear()
    val conf = if (hidden) {
      mainBinding.navigationView.inflateMenu(R.menu.uncovered_menu)
      AppBarConfiguration(setOf(R.id.aboutFragment,R.id.deletionSettingsFragment,R.id.logsFragment,R.id.mainFragment,R.id.editPasswordsFragment),drawer)
    }
    else {
      mainBinding.navigationView.inflateMenu(R.menu.decoy_menu)
      AppBarConfiguration(setOf(R.id.mainFragment,R.id.decoyAboutFragment,R.id.profileFragment,R.id.settingsFragment),drawer)
    }
    mainBinding.navigationView.setupWithNavController(controller)
    if (mainBinding.bigLayout==null){
      NavigationUI.setupWithNavController(mainBinding.toolbar2, controller, conf)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {

    return super.onOptionsItemSelected(item)
  }



}
