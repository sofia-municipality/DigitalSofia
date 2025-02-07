/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.pending

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.digital.sofia.NavMainHomeDirections
import com.digital.sofia.R
import com.digital.sofia.data.extensions.callOnMainThread
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.models.common.SdkStatus
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.documents.DocumentsCheckDeliveredUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsCheckSignedUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsGetPendingUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInJob
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.launchWithDispatcher
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.mappers.forms.PendingDocumentUiMapper
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.models.forms.PendingAdapterMarker
import com.digital.sofia.models.forms.PendingDocumentUi
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.ui.fragments.main.pending.list.PendingDataSource
import com.digital.sofia.ui.fragments.registration.confirm.RegistrationConfirmIdentificationFragmentDirections
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlin.concurrent.Volatile

class PendingViewModel(
    private val mapper: PendingDocumentUiMapper,
    private val documentsCheckSignedUseCase: DocumentsCheckSignedUseCase,
    private val documentsCheckDeliveredUseCase: DocumentsCheckDeliveredUseCase,
    private val documentsGetPendingUseCase: DocumentsGetPendingUseCase,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    private val preferences: PreferencesRepository,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
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
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "SigningViewModelTag"
        private const val DELAY_500 = 500L
        private const val PAGE_SIZE = 20
    }

    override val isAuthorizationActive: Boolean = true

    var evrotrustTransactionId: String? = null

    @Volatile
    var pendingDocument: PendingDocumentUi? = null
        private set

    private var checkSignedDocumentJob: Job? = null

    private var checkDeliveredDocumentJob: Job? = null

    private val _openEditUserLiveDataEvent = MutableLiveData<Unit>()
    val openEditUserLiveDataEvent = _openEditUserLiveDataEvent.readOnly()

    private val _openDocumentLiveDataEvent = MutableLiveData<Unit>()
    val openDocumentLiveDataEvent = _openDocumentLiveDataEvent.readOnly()

    private var dataSourceFactory = object : DataSource.Factory<String, PendingAdapterMarker>() {
        override fun create(): DataSource<String, PendingAdapterMarker> {
            return PendingDataSource(
                viewModelScope = viewModelScope,
                pendingMapper = mapper,
                documentsGetPendingUseCase = documentsGetPendingUseCase,
                showLoader = {
                    hideErrorState()
                    showLoader()
                },
                hideLoader = { hideLoader() },
                showErrorMessage = {
                    if (adapterListLiveData.value?.isEmpty() == true) {
                        showErrorState()
                    } else {
                        showMessage(Message.error(R.string.error_server_error))
                    }
                },
            )
        }
    }

    @Volatile
    var isUserProfileIdentified = true
        private set

    var adapterListLiveData: LiveData<PagedList<PendingAdapterMarker>>
        private set

    init {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(PAGE_SIZE)
            .setPageSize(PAGE_SIZE / 2)
            .build()
        adapterListLiveData = LivePagedListBuilder(dataSourceFactory, config)
            .setBoundaryCallback(object : PagedList.BoundaryCallback<PendingAdapterMarker>() {
                override fun onZeroItemsLoaded() {
                    super.onZeroItemsLoaded()
                    showEmptyState()
                }

                override fun onItemAtEndLoaded(itemAtEnd: PendingAdapterMarker) {
                    super.onItemAtEndLoaded(itemAtEnd)
                    showReadyState()
                }

                override fun onItemAtFrontLoaded(itemAtFront: PendingAdapterMarker) {
                    super.onItemAtFrontLoaded(itemAtFront)
                    showReadyState()
                }
            })
            .build()
    }

    override fun refreshData() {
        logDebug("refreshScreen", TAG)
        pendingDocument?.let { document ->
            when (document.status) {
                DocumentStatusModel.PENDING,
                DocumentStatusModel.SIGNING -> checkSignedDocument()

                DocumentStatusModel.DELIVERING -> checkDeliveredDocument()
                else -> clearData()
            }
        } ?: run {
            viewModelScope.launchWithDispatcher {
                adapterListLiveData.value?.dataSource?.invalidate()
            }
        }
    }

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.SDK_SETUP_ERROR,
            SdkStatus.USER_SETUP_ERROR,
            SdkStatus.CRITICAL_ERROR -> {
                clearData()
                logout()
            }

            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_READY,
            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_REJECTED -> {

                pendingDocument?.let { document ->
                    when (document.status) {
                        DocumentStatusModel.PENDING,
                        DocumentStatusModel.SIGNING -> checkSignedDocument()

                        DocumentStatusModel.DELIVERING -> checkDeliveredDocument()
                        else -> clearData()
                    }
                }
            }

            SdkStatus.USER_STATUS_READY,
            SdkStatus.USER_PROFILE_VERIFIED,
            SdkStatus.ACTIVITY_RESULT_EDIT_PERSONAL_DATA_READY -> {
                isUserProfileIdentified = true
                _openDocumentLiveDataEvent.callOnMainThread()
            }

            SdkStatus.USER_STATUS_NOT_IDENTIFIED -> {
                isUserProfileIdentified = false
                _openEditUserLiveDataEvent.callOnMainThread()
            }

            SdkStatus.USER_PROFILE_PROCESSING,
            SdkStatus.USER_PROFILE_IS_SUPERVISED -> toVerificationWaitFragment()

            SdkStatus.USER_PROFILE_REJECTED -> showErrorMessage(errorMessageRes = R.string.sdk_error_user_profile_rejected)

            SdkStatus.ERROR -> {}
            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_CANCELLED -> {
                pendingDocument?.let { document ->
                    when (document.status) {
                        DocumentStatusModel.DELIVERING -> checkDeliveredDocument()
                        else -> clearData()
                    }
                }
            }

            else -> updateOpenedDocument()
        }
    }

    fun setPendingDocument(document: PendingDocumentUi) {
        pendingDocument = document
    }

    fun showErrorMessage(@StringRes errorMessageRes: Int) = showMessage(
        Message(
            title = StringSource.Res(R.string.oops_with_dots),
            message = StringSource.Res(errorMessageRes),
            type = Message.Type.ALERT,
            positiveButtonText = StringSource.Res(R.string.ok),
            negativeButtonText = StringSource.Res(R.string.cancel),
        )
    )

    private fun updateOpenedDocument() {
        when {
            pendingDocument == null -> logError("updateOpenedDocument pendingDocument isNull", TAG)
            else -> pendingDocument?.let { document ->
                when (document.status) {
                    DocumentStatusModel.SIGNED,
                    DocumentStatusModel.REJECTED -> documentsCheckSignedUseCase.invoke(
                        evrotrustTransactionId = document.evrotrustTransactionId
                    ).onEach { result ->
                        result.onLoading {
                            logDebug("updateOpenedDocument onLoading", TAG)
                            showLoader()
                        }.onSuccess {
                            logDebug("updateOpenedDocument onSuccess", TAG)
                            delay(DELAY_500)
                            hideLoader()
                            clearData()
                            refreshData()
                        }.onRetry {
                            updateOpenedDocument()
                        }.onFailure {
                            logError("updateOpenedDocument onFailure", it, TAG)
                            hideLoader()
                        }
                    }.launchInScope(viewModelScope)

                    DocumentStatusModel.GENERATED -> documentsCheckDeliveredUseCase.invoke(
                        evrotrustThreadId = document.evrotrustThreadId ?: return
                    ).onEach { result ->
                        result.onLoading {
                            logDebug("updateOpenedDocument onLoading", TAG)
                            showLoader()
                        }.onSuccess {
                            logDebug("updateOpenedDocument onSuccess", TAG)
                            delay(DELAY_500)
                            hideLoader()
                            clearData()
                            refreshData()
                        }.onRetry {
                            updateOpenedDocument()
                        }.onFailure {
                            logError("updateOpenedDocument onFailure", it, TAG)
                            hideLoader()
                        }
                    }.launchInScope(viewModelScope)

                    else -> {}
                }
            }
        }
    }

    private fun checkSignedDocument() {
        when {
            pendingDocument == null -> logError("checkSignedDocument pendingDocument isNull", TAG)
            else -> {
                checkSignedDocumentJob?.cancel()
                checkSignedDocumentJob = documentsCheckSignedUseCase.invoke(
                    evrotrustTransactionId = pendingDocument?.evrotrustTransactionId ?: return
                ).onEach { result ->
                    result.onLoading {
                        logDebug("checkSignedDocument onLoading", TAG)
                        showLoader()
                    }.onSuccess {
                        logDebug("checkSignedDocument onSuccess", TAG)
                        delay(DELAY_500)
                        hideLoader()
                        clearData()
                        refreshData()
                    }.onFailure {
                        logError("checkSignedDocument onFailure", it, TAG)
                        viewModelScope.launchWithDispatcher {
                            delay(DELAY_500)
                            hideLoader()
                            hideErrorState()
                            clearData()
                            showMessage(Message.error(it.serverMessage ?: ""))
                            refreshData()
                        }
                    }
                }.launchInJob(viewModelScope)
            }
        }
    }

    private fun checkDeliveredDocument() {
        when {
            pendingDocument == null -> logError(
                "checkDeliveredDocument pendingDocument isNull",
                TAG
            )

            else -> {
                checkDeliveredDocumentJob?.cancel()
                checkDeliveredDocumentJob = documentsCheckDeliveredUseCase.invoke(
                    evrotrustThreadId = pendingDocument?.evrotrustThreadId ?: return
                ).onEach { result ->
                    result.onLoading {
                        logDebug("checkDeliveredDocument onLoading", TAG)
                        showLoader()
                    }.onSuccess {
                        logDebug("checkDeliveredDocument onSuccess", TAG)
                        delay(DELAY_500)
                        hideLoader()
                        clearData()
                        refreshData()
                    }.onFailure {
                        logError("checkDeliveredDocument onFailure", it, TAG)
                        viewModelScope.launchWithDispatcher {
                            delay(DELAY_500)
                            hideLoader()
                            hideErrorState()
                            clearData()
                            showMessage(Message.error(it.serverMessage ?: ""))
                            refreshData()
                        }
                    }
                }.launchInJob(viewModelScope)
            }
        }
    }

    private fun clearData() {
        pendingDocument = null
    }

    override fun onNewPendingDocumentEvent(isNotificationEvent: Boolean) {
        if (!isNotificationEvent) {
            refreshData()
            clearNewPendingDocumentEvent()
        }
    }

    override fun onNewSignedDocumentEvent(isNotificationEvent: Boolean) {
        clearData()
    }

    private fun toVerificationWaitFragment() {
        logDebug("toVerificationWaitFragment", TAG)
        preferences.saveAppStatus(AppStatus.PROFILE_VERIFICATION_REGISTRATION)
        findFlowNavController().navigateInMainThread(
            NavMainHomeDirections.toProfileVerificationWaitFlowFragment(),
            viewModelScope
        )
    }
}