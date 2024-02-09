package com.example.cmd.presentation.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cmd.R
import com.example.cmd.databinding.FilesSettingsFragmentBinding
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.launchLifecycleAwareCoroutine
import com.example.cmd.presentation.MainActivity
import com.example.cmd.presentation.actions.DeletionSettingsAction
import com.example.cmd.presentation.adapter.fileAdapter.MyFileAdapter
import com.example.cmd.presentation.dialogs.InputDigitDialog
import com.example.cmd.presentation.dialogs.QuestionDialog
import com.example.cmd.presentation.states.DeletionActivationStatus
import com.example.cmd.presentation.states.DeletionDataState
import com.example.cmd.presentation.states.DeletionSettingsState
import com.example.cmd.presentation.states.Page
import com.example.cmd.presentation.dialogs.DialogLauncher
import com.example.cmd.presentation.viewmodels.FilesSettingsVM
import com.example.cmd.presentation.viewmodels.FilesSettingsVM.Companion.CONFIRM_AUTODELETION_REQUEST
import com.example.cmd.presentation.viewmodels.FilesSettingsVM.Companion.CONFIRM_CLEAR_REQUEST
import com.example.cmd.presentation.viewmodels.FilesSettingsVM.Companion.TIMEOUT_NOT_READY_REQUEST
import com.example.cmd.presentation.viewmodels.FilesSettingsVM.Companion.XIAOMI_NOTIFICATION_REQUEST
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import javax.inject.Inject


//Фикс бага Recycler View: https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
class LinearLayoutManagerWrapper(context: Context?) : LinearLayoutManager(context) {

  override fun supportsPredictiveItemAnimations(): Boolean {
    return false
  }
}

@AndroidEntryPoint
class DeletionSettingsFragment : Fragment() {

  @Inject
  lateinit var myFileAdapter: MyFileAdapter

  private val viewModel: FilesSettingsVM by viewModels()
  private var _binding: FilesSettingsFragmentBinding? = null
  private val binding
    get() = _binding ?: throw RuntimeException("DeletionSettingsFragmentBinding == null")
  private var allFabsVisible = true
  private val contentResolver by lazy { requireContext().contentResolver }
  private val dpi by lazy { resources.displayMetrics.density }
  private val takeFlags by lazy {
    Intent.FLAG_GRANT_READ_URI_PERMISSION or
      Intent.FLAG_GRANT_WRITE_URI_PERMISSION
  }


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding =
      FilesSettingsFragmentBinding.inflate(inflater, container, false)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewmodel = viewModel
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    //настройки recyclerview.
    setupRecyclerView()

    //Настройка FAB
    setupFABs()

    //Настройка сортировки списка
    setupSort()

    //Настройка ActionBar
    setupActionBar()

    //Настроить автообновление списка файлов
    setupFilesListListener()

    //Обработка результатов диалогов с пользователем
    setupDialogListeners()

    //Обработка событий
    setupActionsListener()

    setMainActivityState()
  }

  private fun setMainActivityState() {
    (activity as MainActivity).setPage(Page.SetupDeletion())
  }

  private fun setupActionsListener() {
    val dialogLauncher = DialogLauncher(parentFragmentManager, context)
    viewLifecycleOwner.launchLifecycleAwareCoroutine {
      viewModel.deletionSettingsActionFlow.collect {
        when (it) {
          is DeletionSettingsAction.ShowUsualDialog -> dialogLauncher.launchDialogFromAction(it.value)
          is DeletionSettingsAction.ShowPriorityEditor -> {
            with(it) {
              showPriorityEditor(
                title.asString(context),
                hint,
                message.asString(context),
                uri,
                range
              )
            }
          }
        }
      }
    }
  }

  private fun showPriorityEditor(
    title: String,
    hint: String,
    message: String,
    uri: String,
    range: IntRange
  ) {
    InputDigitDialog.showPriorityEditor(parentFragmentManager, title, hint, message, uri, range)
  }


  private fun setupFilesListListener() {
    viewLifecycleOwner.launchLifecycleAwareCoroutine {
      viewModel.autoDeletionDataState.collect {
        if (it is DeletionDataState.ViewData) {
          myFileAdapter.submitList(it.items)
        }
      }
    }
  }

  private fun setupDialogListeners() {
    InputDigitDialog.setupEditPriorityListener(
      parentFragmentManager,
      viewLifecycleOwner
    ) { uri: Uri, priority: Int ->
      viewModel.changeFilePriority(priority, uri)
    }
    InputDigitDialog.setupListener(parentFragmentManager, viewLifecycleOwner) {
      viewModel.changeAutodeletionTimeout(it)
    }
    QuestionDialog.setupListener(
      parentFragmentManager,
      TIMEOUT_NOT_READY_REQUEST,
      viewLifecycleOwner
    ) {
      viewModel.showInputTimeoutDialog()
    }
    QuestionDialog.setupListener(
      parentFragmentManager,
      XIAOMI_NOTIFICATION_REQUEST,
      viewLifecycleOwner
    ) {
      showXiaomiPermissionRequest()
      viewModel.xiaomiNotificationSent()
      viewModel.switchAutoDeletionStatus()
    }
    QuestionDialog.setupListener(
      parentFragmentManager,
      CONFIRM_AUTODELETION_REQUEST,
      viewLifecycleOwner
    ) {
      viewModel.switchAutoDeletionStatus()
    }
    QuestionDialog.setupListener(
      parentFragmentManager,
      CONFIRM_CLEAR_REQUEST,
      viewLifecycleOwner
    ) {
      viewModel.clearFilesDb()
    }
  }

  private fun setupSort() {
    binding.sort.setOnClickListener {
      showSortingMenu()
    }
  }

  private fun MyFileAdapter.setRecyclerViewListeners() {
    onMoreClickListener = { viewModel.showFileInfo(it) }
    onDeleteItemClickListener = { viewModel.removeFileFromDb(it) }
    onEditItemClickListener = { viewModel.showPriorityEditor(it) }
  }

  private fun setupRecyclerView() {
    with(binding.items) {
      layoutManager = LinearLayoutManagerWrapper(context)
      myFileAdapter.setRecyclerViewListeners()
      adapter = myFileAdapter
    }
  }

  private fun setupFABs() {
    changeVisibility()
    binding.add.setOnClickListener {
      changeVisibility()
    }
    setupAddFolderButton()
    setupAddFileButton()
  }

  private fun setupAddFileButton() {
    val fileSelectionLauncher =
      registerForActivityResult(ActivityResultContracts.OpenDocument()) { file ->
        Log.w("uriToDelete", file.toString())
        if (file == null) {
          return@registerForActivityResult
        }
        requireActivity().grantUriPermission(
          requireActivity().packageName,
          file,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(file, takeFlags)
        try {
          viewModel.addFileToDb(file, false)
        } catch (e: Exception) {
          Toast.makeText(requireContext(), getString(R.string.file_removed), Toast.LENGTH_LONG)
            .show()
        }
        changeVisibility()
      }

    binding.addFile.setOnClickListener {
      fileSelectionLauncher.launch(arrayOf("*/*"))
    }
  }

  private fun setupAddFolderButton() {
    val folderSelectionLauncher =
      registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { folder ->
        if (folder == null) {
          return@registerForActivityResult
        }
        requireActivity().grantUriPermission(
          requireActivity().packageName,
          folder,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(folder, takeFlags)
        try {
          viewModel.addFileToDb(folder, true)
        } catch (e: Exception) {
          Toast.makeText(requireContext(), getString(R.string.folder_removed), Toast.LENGTH_LONG)
            .show()
        }
        changeVisibility()
      }
    binding.addFolder.setOnClickListener {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
      intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
      folderSelectionLauncher.launch(intent.data)
    }
  }

  //Смена видимости дополнительных FAB
  private fun changeVisibility() {
    if (allFabsVisible) {
      binding.addFile.hide()
      binding.addFolder.hide()
      binding.add.shrink()
    } else {
      binding.add.extend()
      binding.addFile.show()
      binding.addFolder.show()
    }
    allFabsVisible = !allFabsVisible
  }

  //Сортировка списка
  private fun showSortingMenu() {
    val popup = PopupMenu(context, binding.sort)
    popup.menuInflater.inflate(R.menu.sorting, popup.menu)
    popup.setOnMenuItemClickListener {
      val priority = when (it.itemId) {
        R.id.maxpriority -> {
          binding.sort.text =
            resources.getString(R.string.maxpr)
          FilesSortOrder.PRIORITY_DESC
        }

        R.id.minpriority -> {
          binding.sort.text =
            resources.getString(R.string.minpr)
          FilesSortOrder.PRIORITY_ASC
        }

        R.id.alphabet -> {
          binding.sort.text =
            resources.getString(R.string.alphabet)
          FilesSortOrder.NAME_ASC
        }

        R.id.desalphabet -> {
          binding.sort.text =
            resources.getString(R.string.disalphabet)
          FilesSortOrder.NAME_DESC
        }

        R.id.maxsize -> {
          binding.sort.text =
            resources.getString(R.string.sizebig)
          FilesSortOrder.SIZE_DESC
        }

        R.id.minsize -> {
          binding.sort.text =
            resources.getString(R.string.sizesmall)
          FilesSortOrder.SIZE_ASC
        }

        else -> throw RuntimeException("Wrong priority in priority sorting")
      }
      viewModel.changeSortOrder(priority)
      return@setOnMenuItemClickListener true
    }
    popup.show()
  }


  private fun setupActionBar() {
    requireActivity().addMenuProvider(object : MenuProvider {
      //Отрисовка ActionBar
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        viewLifecycleOwner.launchLifecycleAwareCoroutine {
          menuInflater.inflate(R.menu.autodeletion, menu)
          drawSwitchAutodeletionStatusButton(menu)
        }
      }

      //Реакция на клики по кнопкам в ActionBar
      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
          R.id.help -> viewModel.showHelp()
          R.id.timeout -> viewModel.showInputTimeoutDialog()
          R.id.start -> viewModel.changeAutoDeletionStatus()
          R.id.clear -> viewModel.showClearDialog()
        }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
  }

  private suspend fun drawSwitchAutodeletionStatusButton(menu: Menu) {
    viewModel.autoDeletionSettingsState.collect {
      if (it is DeletionSettingsState.ViewSettings) {
        val icon: Int
        val text: Int
        if (it.status == DeletionActivationStatus.ACTIVE) {
          icon = R.drawable.ic_baseline_pause_24
          text = R.string.disable_logs
        } else {
          icon = R.drawable.ic_baseline_play_arrow_24
          text = R.string.enable_logs
        }
        withContext(Main) {
          val startIcon = menu.findItem(R.id.start)
            ?: throw RuntimeException("Start autodeletion button not found")
          startIcon.setIcon(icon).setTitle(text)
        }
      }
    }
  }

  //Запуск настроек автозагрузки Xiaomi
  private fun showXiaomiPermissionRequest() {
    val intent = Intent()
    intent.component = ComponentName(
      XIAOMI_SECURITY_PACKAGE,
      XIAOMI_REQUEST_CLASS
    )
    try {
      startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(
        context,
        getString(R.string.xiaomi_request_failed, e.message),
        Toast.LENGTH_SHORT
      ).show()
    }
  }


  //Обнуление binding
  override fun onDestroy() {
    _binding = null
    super.onDestroy()
  }

  companion object {
    private const val XIAOMI_SECURITY_PACKAGE = "com.miui.securitycenter"
    private const val XIAOMI_REQUEST_CLASS =
      "com.miui.permcenter.autostart.AutoStartManagementActivity"
  }

}
