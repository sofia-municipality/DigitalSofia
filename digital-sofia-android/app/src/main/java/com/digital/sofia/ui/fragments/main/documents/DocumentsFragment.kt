/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents

import androidx.recyclerview.widget.GridLayoutManager
import com.digital.sofia.databinding.FragmentDocumentsBinding
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.enableChangeAnimations
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.documents.DocumentDownloadModel
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.ui.fragments.main.documents.list.DocumentsAdapter
import com.digital.sofia.utils.DownloadHelper
import com.digital.sofia.utils.PaginationScrollListener
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DocumentsFragment :
    BaseFragment<FragmentDocumentsBinding, DocumentsViewModel>(),
    DocumentsAdapter.ClickListener {

    companion object {
        private const val TAG = "DocumentsFragmentTag"
    }

    override fun getViewBinding() = FragmentDocumentsBinding.inflate(layoutInflater)

    override val viewModel: DocumentsViewModel by viewModel()

    private val adapter: DocumentsAdapter by inject()

    private val downloadHelper: DownloadHelper by inject()

    private val preferences: PreferencesRepository by inject()

    override fun setupView() {
        binding.rvDocuments.adapter = adapter
        adapter.clickListener = this
        binding.rvDocuments.enableChangeAnimations(false)
        binding.rvDocuments.addOnScrollListener(object :
            PaginationScrollListener(binding.rvDocuments.layoutManager as GridLayoutManager) {
            override fun loadMoreItems() {
                viewModel.loadMoreDocuments()
            }

            override fun isLastPage(): Boolean {
                return viewModel.isLastPage()
            }

            override fun isLoading(): Boolean {
                return viewModel.isLoading()
            }

        })
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
        viewModel.adapterList.observe(viewLifecycleOwner) {
            setAdapterData(it)
        }
        downloadHelper.onReadyLiveData.observe(viewLifecycleOwner) {
            showMessage(Message.success("Download completed"))
        }
        downloadHelper.onErrorLiveData.observe(viewLifecycleOwner) {
            showMessage(Message.error("Error download documents"))
        }
    }

    private fun setAdapterData(data: List<DocumentsAdapterMarker>) {
        logDebug("setAdapterData size: ${data.size}", TAG)
        binding.refreshLayout.isRefreshing = false
        adapter.items = data
    }

    override fun onShowFileClicked(downloadModel: DocumentDownloadModel) {
        if (downloadModel.url.isEmpty()) {
            showMessage(Message.error("Document download url is empty"))
            return
        }
        val token = preferences.readAccessToken()?.token
        if (token.isNullOrEmpty()) {
            showMessage(Message.error("Document download token is empty"))
            return
        }
        logDebug("onShowFileClicked url: ${downloadModel.url} token: $token", TAG)
        try {
            val urlWithToken = "${downloadModel.url}?authToken=$token"
            viewModel.openDocument(urlWithToken)
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
        viewModel.refreshData()
    }
}