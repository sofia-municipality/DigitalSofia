package com.digitall.digital_sofia.ui.fragments.main.documents

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.digitall.digital_sofia.databinding.FragmentDocumentsBinding
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.enableChangeAnimations
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.documents.DocumentsAdapterMarker
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.ui.fragments.main.documents.list.DocumentsAdapter
import com.digitall.digital_sofia.utils.DownloadHelper
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsFragment :
    BaseFragment<FragmentDocumentsBinding, DocumentsViewModel>(),
    DocumentsAdapter.ClickListener {

    companion object {
        private const val TAG = "DocumentsFragmentTag"
    }

    override fun getViewBinding() = FragmentDocumentsBinding.inflate(layoutInflater)

    override val viewModel: DocumentsViewModel by viewModel()

    private val adapter: DocumentsAdapter by inject()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    private val downloadHelper: DownloadHelper by inject()

    private val preferences: PreferencesRepository by inject()

    override fun setupView() {
        binding.rvDocuments.adapter = adapter
        adapter.clickListener = this
        binding.rvDocuments.enableChangeAnimations(false)
    }

    override fun setupControls() {
        binding.refreshLayout.setOnRefreshListener(viewModel::refreshScreen)
        binding.errorView.reloadClickListener = {
            logDebug("errorView reloadClickListener", TAG)
            viewModel.refreshScreen()
        }
        binding.emptyStateView.reloadClickListener = {
            logDebug("emptyStateView reloadClickListener", TAG)
            viewModel.refreshScreen()
        }
        binding.customToolbar.settingsClickListener = {
            logDebug("customToolbar settingsClickListener", TAG)
            showSettingsMenu()
        }
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.adapterList.onEach {
            setAdapterData(it)
        }.launch(lifecycleScope)
        downloadHelper.onReadyLiveData.observe(viewLifecycleOwner) {
            showBannerMessage(BannerMessage.success("Download completed"))
        }
        downloadHelper.onErrorLiveData.observe(viewLifecycleOwner) {
            showBannerMessage(BannerMessage.error("Error download documents"))
        }
        evrotrustSDKHelper.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                showBannerMessage(BannerMessage.error(it))
            }
            viewModel.refreshScreen()
        }
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            viewModel.onSdkStatusChanged(it)
        }
    }

    private fun setAdapterData(data: List<DocumentsAdapterMarker>) {
        logDebug("setAdapterData size: ${data.size}", TAG)
        binding.refreshLayout.isRefreshing = false
        adapter.items = data
    }

    override fun onDocumentClicked(evrotrustTransactionId: String) {
        logDebug("onDocumentClicked evrotrustTransactionId: $evrotrustTransactionId", TAG)
        evrotrustSDKHelper.openDocument(
            activity = requireActivity(),
            evrotrustTransactionId = evrotrustTransactionId
        )
    }

    override fun onShowFileClicked(url: String) {
        if (url.isEmpty()) {
            showBannerMessage(BannerMessage.error("Document download url is empty"))
            return
        }
        val token = preferences.readAccessToken()
        if (token.isNullOrEmpty()) {
            showBannerMessage(BannerMessage.error("Document download token is empty"))
            return
        }
        logDebug("onShowFileClicked url: $url token: $token", TAG)
        try {
            val urlWithToken = "$url?authToken=$token"
//            viewModel.openDocumentInWebView(urlWithToken)

//
            val uri = Uri.parse(urlWithToken)
            logDebug("Open url: $urlWithToken", TAG)
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            browserIntent.setPackage("com.android.chrome")
            startActivity(browserIntent)
        } catch (e: Exception) {
            logError("onShowFileClicked Exception: ${e.message}", e, TAG)
            showBannerMessage(BannerMessage.error("Error open document, may be download url is empty or wrong"))
        }
    }

    override fun onDownloadFileClicked(url: String) {
        viewModel.onDownloadClicked(requireContext(), url)
    }

}