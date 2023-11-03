package com.digitall.digital_sofia.ui.fragments.main.documents

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.domain.extensions.readOnly
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.common.SdkStatus
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCase
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.mappers.documents.DocumentsUiMapper
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.documents.DocumentsAdapterMarker
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.DownloadHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsViewModel(
    private val mapper: DocumentsUiMapper,
    private val downloadHelper: DownloadHelper,
    private val documentsUseCase: DocumentsUseCase,
    logoutUseCase: LogoutUseCase,
    preferences: PreferencesRepository,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
) : BaseViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
) {

    companion object {
        private const val TAG = "DocumentsViewModelTag"
    }

    override val needUpdateDocuments: Boolean = true

    private val _adapterList = MutableStateFlow<List<DocumentsAdapterMarker>>(emptyList())
    val adapterList = _adapterList.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        subscribeToDocuments()
    }

    fun refreshScreen() {
        logDebug("refreshScreen", TAG)
        updateDocuments()
    }

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.SDK_SETUP_ERROR,
            SdkStatus.USER_SETUP_ERROR,
            SdkStatus.CRITICAL_ERROR -> {
                logout()
            }

            else -> {
                // no action
            }
        }
    }

//    fun openDocumentInWebView(url: String) {
//        findFlowNavController().navigateInMainThread(
//            DocumentsFragmentDirections.toDocumentPreviewFragment(
//                url = url
//            ),
//            viewModelScope
//        )
//    }

    private fun updateDocuments() {
        logDebug("updateDocuments", TAG)
        documentsUseCase.updateDocuments().onEach { result ->
            result.onLoading {
                logDebug("updateDocuments onLoading", TAG)
                if (adapterList.value.isEmpty()) {
                    showLoader()
                }
            }.onSuccess {
                logDebug("updateDocuments onSuccess", TAG)
                hideLoader()
                if (adapterList.value.isEmpty()) {
                    hideErrorState()
                    showEmptyState()
                }
            }.onFailure {
                logError("updateDocuments onFailure", TAG)
                hideLoader()
                if (adapterList.value.isEmpty()) {
                    showErrorState(showReloadButton = true)
                } else {
                    showBannerMessage(BannerMessage.error("Error update documents"))
                }
            }
        }.launch(viewModelScope)
    }

    private fun subscribeToDocuments() {
        logDebug("subscribeToDocuments", TAG)
        documentsUseCase.subscribeToDocuments().onEach {
            val filteredList = it.filter {
                it.status != DocumentStatusModel.SIGNING
            }
            if (filteredList.isEmpty()) {
                logDebug("subscribeToDocuments isEmpty", TAG)
                showEmptyState()
            } else {
                logDebug("subscribeToDocuments isNotEmpty size: ${it.size}", TAG)
                showReadyState()
                _adapterList.value = mapper.mapList(filteredList)
            }
        }.launch(viewModelScope)
    }

    fun onDownloadClicked(
        context: Context,
        url: String,
    ) {
        logDebug("onDownloadClicked url: $url", TAG)
        downloadHelper.downloadFile(
            context = context,
            url = url,
        )
    }
}