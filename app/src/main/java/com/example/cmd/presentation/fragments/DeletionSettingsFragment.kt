package com.example.cmd.presentation.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cmd.R
import com.example.cmd.databinding.DeletionSettingsFragmentBinding
import com.example.cmd.domain.entities.FilesSortOrder
import com.example.cmd.presentation.adapter.MyFileAdapter
import com.example.cmd.presentation.adapter.MyTouchHelper
import com.example.cmd.presentation.dialogs.InfoDialog
import com.example.cmd.presentation.dialogs.InputDigitDialog
import com.example.cmd.presentation.dialogs.QuestionDialog
import com.example.cmd.presentation.states.DeletionActivationStatus
import com.example.cmd.presentation.states.DeletionSettingsState
import com.example.cmd.presentation.viewmodels.DeletionSettingsVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
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

  private val viewModel: DeletionSettingsVM by viewModels()
  private var _binding: DeletionSettingsFragmentBinding? = null
  private val binding
    get() = _binding ?: throw RuntimeException("DeletionSettingsFragmentBinding == null")
  private val controller by lazy { findNavController() }
  private var allFabsVisible = true //вынести в ViewModel?
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
      DeletionSettingsFragmentBinding.inflate(inflater, container, false)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewmodel = viewModel
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.items.layoutManager = LinearLayoutManagerWrapper(context)
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

  }

  private fun setupFilesListListener() {
    lifecycleScope.launch {
      viewModel.filesState.collect {
        myFileAdapter.submitList(it)
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
      showAutoDeletionTimeoutDialog(viewModel.autoDeletionDataState.value)
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
    QuestionDialog.setupListener(parentFragmentManager, CONFIRM_CLEAR_REQUEST, viewLifecycleOwner) {
      viewModel.clearFilesDb()
    }
  }

  private fun setupSort() {
    binding.sort.setOnClickListener {
      showSortingMenu()
    }
  }

  private fun MyFileAdapter.setRecyclerViewListeners() {
    this.onItemLongClickListener = {
      InfoDialog.show(
        parentFragmentManager,
        getString(R.string.about_file_title),
        getString(R.string.fileDetails, it.path, it.sizeFormated, it.priority)
      )
    }
    this.onDeleteItemClickListener = { viewModel.removeFileFromDb(it) }
    this.onEditItemClickListener = {
      InputDigitDialog.showPriorityEditor(
        parentFragmentManager,
        getString(R.string.changePriority),
        it.priority.toString(),
        getString(R.string.file, it.path),
        it.uri.toString(),
        0..10000
      )
    }
  }

  private fun setupRecyclerView() {
    with(binding.items) {
      myFileAdapter.setRecyclerViewListeners()
      adapter = myFileAdapter
      val itemTouchHelper = ItemTouchHelper(MyTouchHelper(dpi))
      itemTouchHelper.attachToRecyclerView(this)
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
        requireActivity().grantUriPermission(
          requireActivity().packageName,
          file,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(file!!, takeFlags)
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
        requireActivity().grantUriPermission(
          requireActivity().packageName,
          folder,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(folder!!, takeFlags)
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

  private fun showAutoDeletionTimeoutDialog(value: DeletionSettingsState) {
    if (value is DeletionSettingsState.ViewData) {
      InputDigitDialog.show(
        parentFragmentManager,
        getString(R.string.timeout_please),
        value.timeout.toString(),
        getString(R.string.timeout_long),
        1..1000
      )
    }
  }

  private fun changeAutoDeletionStatus(value: DeletionSettingsState) {
    if (value is DeletionSettingsState.ViewData) {
      when (value.status) {
        DeletionActivationStatus.ACTIVE -> viewModel.switchAutoDeletionStatus()
        DeletionActivationStatus.INACTIVE_AND_WITHOUT_TIMEOUT -> showAutoDeletionTimeoutNotReadyDialog()
        DeletionActivationStatus.INACTIVE_AND_NOT_NOTIFIED_XIAOMI -> showXiaomiPermissionDialog()
        DeletionActivationStatus.INACTIVE_AND_READY -> showConfirmAutodeletionDialog(value.timeout)
      }
    }
  }

  private fun showXiaomiPermissionDialog() {
    QuestionDialog.show(
      parentFragmentManager,
      getString(R.string.are_you_sure_autodelete),
      getString(R.string.stupid_chinese_phone),
      XIAOMI_NOTIFICATION_REQUEST
    )
  }

  private fun showConfirmAutodeletionDialog(timeOut: Int) {
    QuestionDialog.show(
      parentFragmentManager,
      getString(R.string.are_you_sure_autodelete),
      getString(R.string.final_explanation, timeOut),
      CONFIRM_AUTODELETION_REQUEST
    )
  }

  private fun showAutoDeletionTimeoutNotReadyDialog() {
    QuestionDialog.show(
      parentFragmentManager,
      getString(R.string.please_set_timeout1),
      getString(R.string.please_set_timeout2),
      TIMEOUT_NOT_READY_REQUEST
    )
  }

  private fun setupActionBar() {
    requireActivity().addMenuProvider(object : MenuProvider {
      //Отрисовка ActionBar
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.autodeletion, menu)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.autodelete)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        lifecycleScope.launch {
          drawSwitchAutodeletionStatusButton(menu)
        }
      }

      //Реакция на клики по кнопкам в ActionBar
      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
          R.id.help -> InfoDialog.show(
            parentFragmentManager, getString(R.string.help),
            getString(R.string.long_help)
          )

          R.id.timeout -> showAutoDeletionTimeoutDialog(viewModel.autoDeletionDataState.value)
          R.id.start -> changeAutoDeletionStatus(viewModel.autoDeletionDataState.value)
          R.id.clear -> QuestionDialog.show(
            parentFragmentManager,
            getString(R.string.apply),
            getString(R.string.want_to_clear),
            CONFIRM_CLEAR_REQUEST
          )

          android.R.id.home -> controller.popBackStack()
        }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
  }

  private suspend fun drawSwitchAutodeletionStatusButton(menu: Menu) {
    viewModel.autoDeletionDataState.collect {
      val resource = when (it) {
        is DeletionSettingsState.Loading -> R.drawable.ic_baseline_pause_24
        is DeletionSettingsState.ViewData -> {
          if (it.status == DeletionActivationStatus.ACTIVE) {
            R.drawable.ic_baseline_pause_24
          } else {
            R.drawable.ic_baseline_play_arrow_24
          }
        }
      }
      withContext(Main) {
        val startIcon = menu.findItem(R.id.start)
          ?: throw RuntimeException("Start autodeletion button not found")
        startIcon.setIcon(resource)
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
    _binding=null
    super.onDestroy()
  }

  companion object {
    private const val TIMEOUT_NOT_READY_REQUEST = "set_timeout_request"
    private const val CONFIRM_AUTODELETION_REQUEST = "confirm_autodeletion_start"
    private const val CONFIRM_CLEAR_REQUEST = "confirm_clear_request"
    private const val XIAOMI_NOTIFICATION_REQUEST = "show_xiaomi_notification"
    private const val XIAOMI_SECURITY_PACKAGE = "com.miui.securitycenter"
    private const val XIAOMI_REQUEST_CLASS =
      "com.miui.permcenter.autostart.AutoStartManagementActivity"
  }

}
