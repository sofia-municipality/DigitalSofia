/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.signing

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
import com.digital.sofia.domain.usecase.documents.DocumentsGetUnsignedUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsSendSignedUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInJob
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.mappers.forms.UnsignedDocumentUiMapper
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.forms.HomeAdapterMarker
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SigningViewModel(
    private val mapper: UnsignedDocumentUiMapper,
    private val documentsSendSignedUseCase: DocumentsSendSignedUseCase,
    private val documentsGetUnsignedUseCase: DocumentsGetUnsignedUseCase,
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
        private const val TAG = "SigningViewModelTag"
        private const val UPDATE_DOCUMENTS_COUNT = 16
        private const val UPDATE_DOCUMENTS_TIMEOUT = 5000L
    }

    override val isAuthorizationActive: Boolean = true

    var evrotrustTransactionId: String? = null

    private var sendSignedDocumentJob: Job? = null

    private var counter = 0

    private val _adapterList = MutableLiveData<List<HomeAdapterMarker>>(emptyList())
    val adapterList = _adapterList.readOnly()

    @Volatile
    private var cursor: String? = null

    @Volatile
    private var isLoading = false

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        loadInitialUnsignedDocuments()
    }

    override fun refreshData() {
        logDebug("refreshScreen", TAG)
        if (evrotrustTransactionId.isNullOrEmpty()) {
            loadInitialUnsignedDocuments()
        } else {
            sendSignedDocument()
        }
    }

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.SDK_SETUP_ERROR,
            SdkStatus.USER_SETUP_ERROR,
            SdkStatus.CRITICAL_ERROR -> {
                evrotrustTransactionId = null
                logout()
            }

            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_READY,
            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_REJECTED -> {
                counter = 0
                sendSignedDocument()
            }

            else -> {
                updateOpenedDocument()
            }
        }
    }

    fun isLastPage(): Boolean {
        return  cursor.isNullOrEmpty()
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    fun loadMoreDocuments() {
        logDebug("loadMoreDocuments", TAG)
        isLoading = true
        documentsGetUnsignedUseCase.invoke(cursor = cursor).onEach { result ->
            result.onLoading {
                logDebug("loadMoreDocuments onLoading", TAG)
            }.onSuccess {
                logDebug("loadMoreDocuments onSuccess", TAG)
                isLoading = false
                if (it.documents.isNotEmpty()) {
                    _adapterList.setValueOnMainThread(_adapterList.value?.plus(mapper.mapList(it.documents)))
                }
                cursor = it.cursor
            }.onRetry {
                loadMoreDocuments()
            }.onFailure {
                logError("loadMoreDocuments onFailure", it, TAG)
                isLoading = false
                showMessage(Message.error(R.string.error_server_error))
            }
        }.launchInScope(viewModelScope)
    }

    private fun updateOpenedDocument() {
        if (evrotrustTransactionId.isNullOrEmpty()) {
            logError("updateOpenedDocument evrotrustTransactionId.isNullOrEmpty()", TAG)
            showMessage(Message.error(R.string.error_server_error))
            return
        }
        documentsSendSignedUseCase.invoke(
            evrotrustTransactionId = evrotrustTransactionId!!
        ).onEach { result ->
            result.onLoading {
                logDebug("updateOpenedDocument onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("updateOpenedDocument onSuccess", TAG)
                refreshData()
            }.onRetry {
                updateOpenedDocument()
            }.onFailure {
                logError("updateOpenedDocument onFailure", it, TAG)
                hideLoader()
            }
        }.launchInJob(viewModelScope)
    }

    private fun sendSignedDocument() {
        if (evrotrustTransactionId.isNullOrEmpty()) {
            logError("sendSignedDocument evrotrustTransactionId.isNullOrEmpty()", TAG)
            showMessage(Message.error(R.string.error_server_error))
            return
        }
        sendSignedDocumentJob?.cancel()
        sendSignedDocumentJob = documentsSendSignedUseCase.invoke(
            evrotrustTransactionId = evrotrustTransactionId!!
        ).onEach { result ->
            result.onLoading {
                logDebug("sendSignedDocument onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("sendSignedDocument onSuccess", TAG)
                counter = 0
                evrotrustTransactionId = null
                refreshData()
            }.onRetry {
                sendSignedDocument()
            }.onFailure {
                logError("sendSignedDocument onFailure", it, TAG)
                if (counter >= UPDATE_DOCUMENTS_COUNT) {
                    hideLoader()
                    showErrorState()
                } else {
                    viewModelScope.launch {
                        delay(UPDATE_DOCUMENTS_TIMEOUT)
                        sendSignedDocument()
                    }
                }
            }
        }.launchInJob(viewModelScope)
    }

    private fun loadInitialUnsignedDocuments() {
        logDebug("subscribeToUnsignedDocuments", TAG)
        isLoading = true
        showReadyState()
        documentsGetUnsignedUseCase.invoke(cursor = null).onEach { result ->
            result.onLoading {
                logDebug("subscribeToUnsignedDocuments onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("subscribeToUnsignedDocuments onSuccess", TAG)
                isLoading = false
                hideLoader()
                setHasNewUnsignedDocuments(it.documents.isNotEmpty())
                if (it.documents.isEmpty()) {
                    showEmptyState()
                } else {
                    _adapterList.setValueOnMainThread(mapper.mapList(it.documents))
                    cursor = it.cursor
                }
            }.onRetry {
                loadInitialUnsignedDocuments()
            }.onFailure {
                logError("subscribeToUnsignedDocuments onFailure", it, TAG)
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

    override fun onNewPendingDocumentEvent(isNotificationEvent: Boolean) {
        if (!isNotificationEvent) {
            refreshData()
            clearNewPendingDocumentEvent()
        }
    }
}