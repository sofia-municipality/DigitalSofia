/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents.preview

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.data.DOCUMENT_PDF_FILE_NAME
import com.digital.sofia.domain.extensions.readOnly
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.DownloadProgress
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.documents.DocumentsDownloadDocumentUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import java.io.File

class DocumentPreviewViewModel(
    private val currentContext: CurrentContext,
    private val documentsDownloadDocumentUseCase: DocumentsDownloadDocumentUseCase,
    private val preferences: PreferencesRepository,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
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
        private const val TAG = "DocumentPreviewViewModelTag"
    }

    override val isAuthorizationActive: Boolean = true

    private val _documentPdfLiveData = MutableStateFlow<File?>(null)
    val documentPdfLiveData = _documentPdfLiveData.readOnly()

    private var documentFormIOId: String? = null

    fun setDocumentFormIOId(documentFormIOId: String) {
        this.documentFormIOId = documentFormIOId
    }

    fun downloadDocument() {
        logDebug("downloadFile documentFormIOId: $documentFormIOId", TAG)
        if (documentFormIOId.isNullOrEmpty()) {
            showErrorState(
                description = StringSource.Text("Document url not found"),
            )
            return
        }

        try {
            val directory = currentContext.get().cacheDir!!
            if (!directory.exists()) {
                val created = directory.mkdir()
                if (!created) {
                    logError("downloadFile directory not created", TAG)
                    showErrorState(
                        description = StringSource.Text("Error create directory"),
                    )
                    return
                }
            }
            val file = File(directory, DOCUMENT_PDF_FILE_NAME)
            if (!file.exists()) {
                val created = file.createNewFile()
                if (!created) {
                    logError("downloadFile file not created", TAG)
                    showErrorState(
                        description = StringSource.Text("Error create file"),
                    )
                    return
                }
            }
            documentsDownloadDocumentUseCase.invoke(
                file = file,
                documentFormIOId = documentFormIOId ?: return,
            ).onEach { result ->
                result.onLoading {
                    logDebug("downloadFile onLoading", TAG)
                    showLoader()
                }.onSuccess {
                    when (it) {
                        is DownloadProgress.Loading -> {
                            logDebug("downloadFile onLoading percents: ${it.message}", TAG)
                            showLoader(it.message)
                        }

                        is DownloadProgress.Ready -> {
                            logDebug(
                                "downloadFile onSuccess percents: $it\nfile: ${file.absoluteFile}",
                                TAG
                            )
                            hideLoader()
                            _documentPdfLiveData.value = file
                        }
                    }
                }.onRetry {
                    downloadDocument()
                }.onFailure {
                    logError("downloadFile onFailure", it, TAG)
                    hideLoader()
                    showErrorState(
                        title = StringSource.Res(R.string.error_unexpected),
                        description = StringSource.Res(R.string.error_file_download),
                    )
                }
            }.launchInScope(viewModelScope)

        } catch (e: Exception) {
            logError("downloadFile Exception: ${e.message}", e, TAG)
            showErrorState(
                description = StringSource.Text("Error open file"),
            )
        }
    }
}