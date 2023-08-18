package com.example.cmd.presentation.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.anggrayudi.storage.extension.isMediaDocument
import com.example.cmd.R
import com.example.cmd.presentation.adapter.MyTouchHelper
import com.example.cmd.databinding.DeletionSettingsBinding
import com.example.cmd.presentation.factory.DeletionFactory
import com.example.cmd.helpers.DialogHelper
import com.example.cmd.helpers.Request
import com.example.cmd.model.MySortedList
import com.example.cmd.presentation.viewmodels.DeletionSettingsVM


//Фикс бага Recycler View: https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
class LinearLayoutManagerWrapper(context: Context?) : LinearLayoutManager(context) {

  override fun supportsPredictiveItemAnimations(): Boolean {
    return false
  }
}

class DeletionSettingsFragment : Fragment() {
  private val dpi by lazy { resources.displayMetrics.density }
  private val viewModel: DeletionSettingsVM by viewModels {
    DeletionFactory(requireContext())
  }
  private lateinit var deletionSettingsBinding: DeletionSettingsBinding
  private val controller by lazy { findNavController() }
  private val dialogHelper by lazy { DialogHelper(controller) }
  private var allFabsVisible = false
  private val contentResolver by lazy { requireContext().contentResolver }
  private val takeFlags by lazy {
    Intent.FLAG_GRANT_READ_URI_PERMISSION or
      Intent.FLAG_GRANT_WRITE_URI_PERMISSION
  }


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    deletionSettingsBinding =
      DeletionSettingsBinding.inflate(inflater, container, false)
    deletionSettingsBinding.items.layoutManager = LinearLayoutManagerWrapper(context)
    deletionSettingsBinding.lifecycleOwner = this
    deletionSettingsBinding.viewmodel = viewModel
    //настройки recyclerview. Получение адаптера из viewmodel.
    ItemTouchHelper(MyTouchHelper(dpi)).attachToRecyclerView(deletionSettingsBinding.items)
    //скрытие дополнительных FAB
    deletionSettingsBinding.addFile.visibility = View.GONE
    deletionSettingsBinding.addFolder.visibility = View.GONE
    deletionSettingsBinding.add.shrink()

    viewModel.loaded.observe(requireActivity()) {
      deletionSettingsBinding.items.adapter = it
    }
    //Слушатели кликов по кнопкам
    deletionSettingsBinding.sort.setOnClickListener {
      menu(R.id.sort)
    }

    deletionSettingsBinding.sort1.setOnClickListener {
      menu(R.id.sort1)
    }

    deletionSettingsBinding.add.setOnClickListener {
      addItem()
    }

    val folderSelectionLauncher =
      registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { folder ->
        requireActivity().grantUriPermission(
          requireActivity().packageName,
          folder,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(folder!!, takeFlags)
        try {
          viewModel.addFile(folder, false)
        } catch (e: Exception) {
          Toast.makeText(requireContext(), getString(R.string.folder_removed), Toast.LENGTH_LONG).show()
        }
        changeVisibility()
      }

    val fileSelectionLauncher =
      registerForActivityResult(ActivityResultContracts.OpenDocument()) { file ->
        requireActivity().grantUriPermission(
          requireActivity().packageName,
          file,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(file!!, takeFlags)
        val media = file.isMediaDocument
        if (media && viewModel.version in 30..32)
          dialogHelper.infoDialog(
            getString(R.string.file_removed),
            getString(R.string.problematic_file,file.path)
          )
        else {
          try {
          viewModel.addFile(file, media)
          } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.file_removed), Toast.LENGTH_LONG).show()
          }
        }
        changeVisibility()
      }

    deletionSettingsBinding.addFolder.setOnClickListener {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
      intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
      folderSelectionLauncher.launch(intent.data)
    }
    deletionSettingsBinding.addFile.setOnClickListener {
      fileSelectionLauncher.launch(arrayOf("*/*"))
    }

    viewModel.back.observe(requireActivity()) {
      controller.popBackStack()
    }
    //Обработка событий из ViewModel
    viewModel.action.observe(requireActivity()) {
      when (it.get()?.type) {
        Request.Type.ALERT ->
          dialogHelper.infoDialog(it.list[0], it.list[1])
        Request.Type.QUESTION ->
          dialogHelper.questionDialog(it.list[0], it.list[1], it.list[2])
        Request.Type.TIMEOUT -> dialogHelper.inputDialog(
          "putTimeOut",
          it.list[0],
          it.list[1],
          it.list[2],
          dpi
        )
        Request.Type.PRIORITY -> dialogHelper.inputDialog(
          "editPriority",
          it.list[0],
          it.list[1],
          it.list[2],
          dpi
        )
        Request.Type.TOAST ->
          Toast.makeText(
            context,
            it.list[0],
            Toast.LENGTH_LONG
          ).show()

        else -> {}
      }
    }

    //Обработка результатов диалогов с пользователем
    setFragmentResultListener(
      "request"
    ) { _, result ->
      when (result.getString("response")) {
        "clear" -> viewModel.clear()
        "chinese" -> {
          chinese()
          viewModel.launch(true)
        }
        "start" -> viewModel.launch()
        "editPriority" -> {
          if (viewModel.editPriority(result.getString("extra") ?: " "))
            controller.popBackStack()
        }
        "putTimeOut" -> {
          if (viewModel.putTimeout(result.getString("extra") ?: " "))
            controller.popBackStack()
        }
        "notification" -> {
          viewModel.notified()
        }
      }
    }
    requireActivity().addMenuProvider(object : MenuProvider {
      //снятие/восстановление маскировки
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.autodelete)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.started.observe(requireActivity()) {
          with(menu.findItem(R.id.start)!!) {
            if (it) {
              setIcon(R.drawable.ic_baseline_pause_24)
            } else {
              setIcon(R.drawable.ic_baseline_play_arrow_24)
            }
          }
        }
      }

      //Открытие других экранов
      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
          R.id.help -> dialogHelper.infoDialog(
            getString(R.string.help),
            getString(R.string.long_help)
          )
          R.id.timeout -> viewModel.timer()
          R.id.start -> viewModel.switch()
          R.id.clear -> dialogHelper.questionDialog(
            "clear",
            getString(R.string.apply),
            getString(R.string.want_to_clear)
          )
          android.R.id.home -> controller.popBackStack()
        }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    return deletionSettingsBinding.root
  }

  //Смена видимости дополнительных FAB, предупреждение пользователю
  private fun addItem() {
    if (!viewModel.notified) {
      if (viewModel.version in 30..32)
        dialogHelper.questionDialog(
          "notification",
          getString(R.string.attention),
          getString(R.string.trouble_with_mediafiles, android.os.Build.MODEL)
        )
    }
    changeVisibility()
  }

  private fun changeVisibility() {
    if (allFabsVisible) {
      deletionSettingsBinding.addFile.hide()
      deletionSettingsBinding.addFolder.hide()
      deletionSettingsBinding.add.shrink()
    } else {
      deletionSettingsBinding.add.extend()
      deletionSettingsBinding.addFile.show()
      deletionSettingsBinding.addFolder.show()
    }
    allFabsVisible = !allFabsVisible
  }

  //Сортировка списка
  private fun menu(id: Int) {
    val popup = PopupMenu(context, requireActivity().findViewById(id))
    popup.menuInflater.inflate(R.menu.menu, popup.menu)
    popup.setOnMenuItemClickListener {
      val priority = when (it.itemId) {
        R.id.maxpriority -> {
          deletionSettingsBinding.sort.text =
            resources.getString(R.string.maxpr)
          MySortedList.Priority.PRIORITY_DESCENDING
        }
        R.id.minpriority -> {
          deletionSettingsBinding.sort.text =
            resources.getString(R.string.minpr)
          MySortedList.Priority.PRIORITY_ASCENDING
        }
        R.id.alphabet -> {
          deletionSettingsBinding.sort.text =
            resources.getString(R.string.alphabet)
          MySortedList.Priority.PATH_ASCENDING
        }
        R.id.desalphabet -> {
          deletionSettingsBinding.sort.text =
            resources.getString(R.string.disalphabet)
          MySortedList.Priority.PATH_DESCENDING
        }
        R.id.maxsize -> {
          deletionSettingsBinding.sort.text =
            resources.getString(R.string.sizebig)
          MySortedList.Priority.SIZE_DESCENDING
        }
        R.id.minsize -> {
          deletionSettingsBinding.sort.text =
            resources.getString(R.string.sizesmall)
          MySortedList.Priority.SIZE_ASCENDING
        }
        else -> null
      }
      viewModel.sort(priority!!)
      return@setOnMenuItemClickListener true
    }
    popup.show()
  }

  //Запуск настроек автозагрузки Xiaomi
  private fun chinese() {
    val intent = Intent()
    intent.component = ComponentName(
      "com.miui.securitycenter",
      "com.miui.permcenter.autostart.AutoStartManagementActivity"
    )
    try {
      startActivity(intent)
    } catch (e: Exception) {

    }
  }


  //сохранение данных при остановке activity
  override fun onStop() {
    viewModel.save()
    super.onStop()
  }

}
