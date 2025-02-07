/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents

import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import com.digital.sofia.databinding.FragmentDocumentsBinding
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.enableChangeAnimations
import com.digital.sofia.extensions.registerChangeStateObserver
import com.digital.sofia.extensions.unregisterChangeStateObserver
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.documents.DocumentDownloadModel
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.ui.fragments.main.documents.list.DocumentsAdapter
import com.digital.sofia.utils.DownloadHelper
import com.digital.sofia.utils.RecyclerViewAdapterDataObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DocumentsFragment :
    BaseFragment<FragmentDocumentsBinding, DocumentsViewModel>(),
    DocumentsAdapter.ClickListener {

    companion object {
        private const val TAG = "DocumentsFragmentTag"
        private const val DELAY_500 = 500L
    }

    override fun getViewBinding() = FragmentDocumentsBinding.inflate(layoutInflater)

    override val viewModel: DocumentsViewModel by viewModel()

    private val adapter: DocumentsAdapter by inject()

    private val downloadHelper: DownloadHelper by inject()

    private val preferences: PreferencesRepository by inject()

    private val adapterDataObserver: RecyclerViewAdapterDataObserver by inject()

    private var updateEmptyStateJob: Job? = null

    override fun setupView() {
        binding.rvDocuments.adapter = adapter
        adapter.clickListener = this
        binding.rvDocuments.enableChangeAnimations(false)
    }

    override fun onCreated() {
        super.onCreated()
        viewModel.clearNewSignedDocumentEvent()
    }

    override fun setupControls() {
        binding.refreshLayout.setOnRefreshListener(viewModel::refreshData)
        binding.errorView.actionOneClickListener = {
            logDebug("errorView reloadClickListener", TAG)
            viewModel.refreshData()
        }
        binding.emptyStateView.reloadClickListener = {
            logDebug("emptyStateView reloadClickListener", TAG)
            viewModel.refreshData()
        }
        binding.customToolbar.settingsClickListener = {
            logDebug("customToolbar settingsClickListener", TAG)
            showSettingsMenu()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.adapterListLiveData.observe(viewLifecycleOwner) {
            setAdapterData(it)
        }
        downloadHelper.onReadyLiveData.observe(viewLifecycleOwner) {
            showMessage(Message.success("Download completed"))
        }
        downloadHelper.onErrorLiveData.observe(viewLifecycleOwner) {
            showMessage(Message.error("Error download documents"))
        }
    }

    private fun setAdapterData(data: PagedList<DocumentsAdapterMarker>) {
        logDebug("setAdapterData size: ${data.size}", TAG)
        binding.refreshLayout.isRefreshing = false
        adapter.submitList(data)
        setupEmptyState(size = data.size)
    }

    private fun setupEmptyState(size: Int?) {
        logDebug("setupEmptyState size: $size", TAG)
        updateEmptyStateJob?.cancel()
        updateEmptyStateJob = lifecycleScope.launch {
            if (size == null || size == 0) {
                delay(DELAY_500)
                showEmptyState()
            } else {
                hideEmptyState()
            }
        }
    }

    override fun onShowFileClicked(downloadModel: DocumentDownloadModel) {
        if (downloadModel.url.isEmpty()) {
            showMessage(Message.error("Document download url is empty"))
            return
        }
        try {
            viewModel.openDocument(documentFormIOId = downloadModel.formioId)
        } catch (e: Exception) {
            logError("onShowFileClicked Exception: ${e.message}", e, TAG)
            showMessage(Message.error("Error open document, may be download url is empty or wrong"))
        }
    }

    override fun onDownloadFileClicked(downloadModel: DocumentDownloadModel) {
        viewModel.onDownloadClicked(requireContext(), downloadModel)
    }

    override fun onResume() {
        super.onResume()
        adapter.registerChangeStateObserver(
            observer = adapterDataObserver,
            changeStateListener = {
                logDebug("adapterDataObserver stateChanged", TAG)
                setupEmptyState(adapter.currentList?.size)
            }
        )
        viewModel.refreshData()
    }

    override fun onPause() {
        super.onPause()
        logDebug("onPause", TAG)
        adapter.unregisterChangeStateObserver(adapterDataObserver)
    }
}