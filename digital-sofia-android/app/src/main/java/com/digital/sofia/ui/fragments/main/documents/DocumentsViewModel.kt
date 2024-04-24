/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.SdkStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.documents.DocumentsGetHistoryUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.mappers.documents.DocumentsUiMapper
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.documents.DocumentDownloadModel
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.DownloadHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.onEach

class DocumentsViewModel(
    private val mapper: DocumentsUiMapper,
    private val downloadHelper: DownloadHelper,
    private val documentsGetHistoryUseCase: DocumentsGetHistoryUseCase,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    preferences: PreferencesRepository,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : BaseViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "DocumentsViewModelTag"
    }

    override val isAuthorizationActive: Boolean = true

    private val _adapterList = MutableLiveData<List<DocumentsAdapterMarker>>(emptyList())
    val adapterList = _adapterList.readOnly()

    @Volatile
    private var cursor: String? = null

    @Volatile
    private var isLoading = false

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        loadInitialDocumentsHistory()
    }

    fun isLastPage(): Boolean {
        return cursor.isNullOrEmpty()
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    override fun refreshData() {
        logDebug("refreshScreen", TAG)
        loadInitialDocumentsHistory()
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

    fun openDocument(documentUrl: String) {
        findFlowNavController().navigateInMainThread(
            DocumentsFragmentDirections.toDocumentPreviewFragment(
                documentUrl = documentUrl
            ),
            viewModelScope
        )
    }

    fun loadMoreDocuments() {
        logDebug("loadMoreDocuments", TAG)
        isLoading = true
        documentsGetHistoryUseCase.invoke(cursor = cursor).onEach { result ->
            result.onLoading {
                logDebug("loadMoreDocuments onLoading", TAG)
            }.onSuccess {
                logDebug("loadMoreDocuments onSuccess", TAG)
                isLoading = false
                if (it.documents.isNotEmpty()) {
                    _adapterList.setValueOnMainThread(_adapterList.value?.plus(mapper.map(it.documents)))
                }
                cursor = it.cursor
            }.onRetry {
                loadMoreDocuments()
            }.onFailure {
                isLoading = false
                logError("loadMoreDocuments onFailure", it, TAG)
                showMessage(Message.error(R.string.error_server_error))
            }
        }.launchInScope(viewModelScope)
    }

    private fun loadInitialDocumentsHistory() {
        logDebug("loadInitialDocumentsHistory", TAG)
        isLoading = true
        showReadyState()
        documentsGetHistoryUseCase.invoke(cursor = null).onEach { result ->
            result.onLoading {
                logDebug("loadInitialDocumentsHistory onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("loadInitialDocumentsHistory onSuccess", TAG)
                hideLoader()
                isLoading = false
                if (it.documents.isEmpty()) {
                    showEmptyState()
                } else {
                    _adapterList.setValueOnMainThread(mapper.mapWithHeader(it.documents))
                    cursor = it.cursor
                }
            }.onRetry {
                loadInitialDocumentsHistory()
            }.onFailure {
                logError("loadInitialDocumentsHistory onFailure", it, TAG)
                isLoading = false
                hideLoader()
                if (adapterList.value?.isEmpty() == true) {
                    showErrorState()
                } else {
                    showMessage(Message.error(R.string.error_server_error))
                }
            }
        }.launchInScope(viewModelScope)
    }

    fun onDownloadClicked(
        context: Context,
        downloadModel: DocumentDownloadModel,
    ) {
        logDebug("onDownloadClicked url: ${downloadModel.url}", TAG)
        downloadHelper.downloadFile(
            context = context,
            downloadModel = downloadModel,
        )
    }

    override fun onNewSignedDocumentEvent(isNotificationEvent: Boolean) {
        if (!isNotificationEvent) {
            refreshData()
            clearNewSignedDocumentEvent()
        }
    }
}