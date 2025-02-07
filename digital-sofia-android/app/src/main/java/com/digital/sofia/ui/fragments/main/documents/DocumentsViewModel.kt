/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents

import android.content.Context
import android.nfc.tech.MifareUltralight.PAGE_SIZE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
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
import com.digital.sofia.extensions.launchWithDispatcher
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.mappers.documents.DocumentsUiMapper
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.documents.DocumentDownloadModel
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.ui.fragments.main.documents.list.DocumentsDataSource
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.DownloadHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
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
        private const val TAG = "DocumentsViewModelTag"
        private const val PAGE_SIZE = 20
    }

    override val isAuthorizationActive: Boolean = true

    private var dataSourceFactory = object : DataSource.Factory<String, DocumentsAdapterMarker>() {
        override fun create(): DataSource<String, DocumentsAdapterMarker> {
            return DocumentsDataSource(
                viewModelScope = viewModelScope,
                documentsMapper = mapper,
                documentsGetHistoryUseCase = documentsGetHistoryUseCase,
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

    var adapterListLiveData: LiveData<PagedList<DocumentsAdapterMarker>>
        private set

    init {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(PAGE_SIZE)
            .setPageSize(PAGE_SIZE / 2)
            .build()
        adapterListLiveData = LivePagedListBuilder(dataSourceFactory, config)
            .setBoundaryCallback(object : PagedList.BoundaryCallback<DocumentsAdapterMarker>() {
                override fun onZeroItemsLoaded() {
                    super.onZeroItemsLoaded()
                    showEmptyState()
                }

                override fun onItemAtEndLoaded(itemAtEnd: DocumentsAdapterMarker) {
                    super.onItemAtEndLoaded(itemAtEnd)
                    showReadyState()
                }

                override fun onItemAtFrontLoaded(itemAtFront: DocumentsAdapterMarker) {
                    super.onItemAtFrontLoaded(itemAtFront)
                    showReadyState()
                }
            })
            .build()
    }

    override fun refreshData() {
        logDebug("refreshScreen", TAG)
        viewModelScope.launchWithDispatcher {
            adapterListLiveData.value?.dataSource?.invalidate()
        }
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

    fun openDocument(documentFormIOId: String) {
        findFlowNavController().navigateInMainThread(
            DocumentsFragmentDirections.toDocumentPreviewFragment(
                documentFormIOId = documentFormIOId
            ),
            viewModelScope
        )
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