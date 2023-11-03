package com.digitall.digital_sofia.ui.fragments.registration.share

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.common.AppStatus
import com.digitall.digital_sofia.domain.models.common.SdkStatus
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCase
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.usecase.registration.RegistrationUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.launchInJob
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.StringSource
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationShareYourDataViewModel(
    private val documentsUseCase: DocumentsUseCase,
    private val preferences: PreferencesRepository,
    private val registrationUseCase: RegistrationUseCase,
    logoutUseCase: LogoutUseCase,
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
        private const val TAG = "RegistrationShareYourDataViewModelTag"
        private const val UPDATE_DOCUMENTS_COUNT = 16
        private const val UPDATE_DOCUMENTS_TIMEOUT = 5000L
    }

    override val needUpdateDocuments: Boolean = false

    private var evrotrustTransactionId: String? = null

    private var getDocumentsJob: Job? = null

    private var counter = 0

    private val _openDocumentViewLiveData = SingleLiveEvent<String>()
    val openDocumentViewLiveData = _openDocumentViewLiveData.readOnly()

    fun updateDocuments() {
        logDebug("updateDocuments", TAG)
        evrotrustTransactionId = preferences.readEvrotrustTransactionIdForLogin()
        val appStatus = preferences.readAppStatus()
        if (evrotrustTransactionId.isNullOrEmpty() ||
            appStatus == AppStatus.NOT_SIGNED_DOCUMENT
        ) {
            logDebug("updateDocuments evrotrustTransactionId.isNullOrEmpty()", TAG)
            counter = 0
            getDocuments()
        }
    }

    fun proceedNext() {
        logDebug("proceedNext", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("proceedNext pinCode == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            toRegistrationFragment()
            return
        }
        if (!pinCode.validate()) {
            logError("proceedNext !pinCode.validate()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            toRegistrationFragment()
            return
        }
        val user = preferences.readUser()
        if (user == null) {
            logError("proceedNext user == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            toRegistrationFragment()
            return
        }
        if (!user.validate()) {
            logError("proceedNext !user.validate())", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            toRegistrationFragment()
            return
        }
        if (evrotrustTransactionId.isNullOrEmpty()) {
            counter = 0
            getDocuments()
            return
        }
        when (preferences.readAppStatus()) {
            AppStatus.NOT_SIGNED_DOCUMENT -> {
                _openDocumentViewLiveData.value = evrotrustTransactionId
            }

            AppStatus.NOT_SEND_SIGNED_DOCUMENT -> {
                sendSignedDocument()
            }

            else -> {
                logError("proceedNext else", TAG)
                showBannerMessage(BannerMessage.error(R.string.error))
                toRegistrationFragment()
            }
        }
    }

    fun onNoClicked() {
        logDebug("onNoClicked", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationShareYourDataFragmentDirections.toRegistrationErrorFragment(
                errorMessage = StringSource.Res(R.string.error_no_consent_to_share_data)
            ),
            viewModelScope
        )
    }

    private fun getDocuments() {
        logDebug("getDocuments", TAG)
        getDocumentsJob?.cancel()
        getDocumentsJob = documentsUseCase.getDocuments().onEach { result ->
            result.onLoading {
                logDebug("getDocuments onLoading", TAG)
                showLoader()
                hideErrorState()
            }.onSuccess {
                logDebug("getDocuments onSuccess", TAG)
                proceedDocuments(it)
            }.onFailure {
                logError("getDocuments onFailure", TAG)
                proceedDocuments()
            }
        }.launchInJob(viewModelScope)
    }

    private fun proceedDocuments(documents: List<DocumentModel>? = null) {
        logDebug("proceedDocuments", TAG)
        counter++
        when {
            documents.isNullOrEmpty() -> {
                logError("proceedDocuments documents.isNullOrEmpty()", TAG)
                if (counter >= UPDATE_DOCUMENTS_COUNT) {
                    hideLoader()
                    showErrorState()
                } else {
                    viewModelScope.launch {
                        delay(UPDATE_DOCUMENTS_TIMEOUT)
                        getDocuments()
                    }
                }
                return
            }

            documents.size != 1 -> {
                logError("proceedDocuments documents.size != 1", TAG)
                if (counter >= UPDATE_DOCUMENTS_COUNT) {
                    hideLoader()
                    showErrorState()
                } else {
                    viewModelScope.launch {
                        delay(UPDATE_DOCUMENTS_TIMEOUT)
                        getDocuments()
                    }
                }
                return
            }

            documents.firstOrNull()?.evrotrustTransactionId.isNullOrEmpty() -> {
                logError(
                    "proceedDocuments documents.first().evrotrustTransactionId.isEmpty()",
                    TAG
                )
                if (counter >= UPDATE_DOCUMENTS_COUNT) {
                    hideLoader()
                    showErrorState()
                } else {
                    viewModelScope.launch {
                        delay(UPDATE_DOCUMENTS_TIMEOUT)
                        getDocuments()
                    }
                }
                return
            }

            else -> {
                evrotrustTransactionId = documents.first().evrotrustTransactionId
                logDebug(
                    "proceedDocuments open evrotrustTransactionId: $evrotrustTransactionId",
                    TAG
                )
                hideLoader()
                hideErrorState()
            }
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

            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_READY -> {
                if (evrotrustTransactionId.isNullOrEmpty()) {
                    logError(
                        "onSdkStatusChanged evrotrustTransactionId.isNullOrEmpty()",
                        TAG
                    )
                    showBannerMessage(BannerMessage.error(R.string.error))
                    toRegistrationFragment()
                    return
                }
                preferences.saveEvrotrustTransactionIdForLogin(evrotrustTransactionId!!)
                preferences.saveAppStatus(AppStatus.NOT_SEND_SIGNED_DOCUMENT)
                counter = 0
                sendSignedDocument()
            }

            else -> {
                // no action
            }
        }
    }

    private fun sendSignedDocument() {
        logDebug("sendSignedDocument", TAG)
        if (evrotrustTransactionId.isNullOrEmpty()) {
            logError("sendSignedDocument evrotrustTransactionId.isNullOrEmpty()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error))
            toRegistrationFragment()
            return
        }
        val refreshToken = preferences.readRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            logError("sendSignedDocument refreshToken.isNullOrEmpty()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error))
            toRegistrationFragment()
            return
        }
        registrationUseCase.sendSignedDocument(
            evrotrustTransactionId = evrotrustTransactionId!!,
            refreshToken = refreshToken,
        ).onEach { result ->
            result.onLoading {
                logDebug("sendSignedDocument onLoading", TAG)
                showLoader()
                hideErrorState()
            }.onSuccess {
                logDebug("sendSignedDocument onSuccess", TAG)
                hideLoader()
                preferences.saveAppStatus(AppStatus.READY)
                toRegistrationReadyFragment()
            }.onFailure {
                logError("sendSignedDocument onFailure", TAG)
                proceedShareDocuments()
            }
        }.launch(viewModelScope)
    }

    private fun proceedShareDocuments() {
        logDebug("proceedShareDocuments", TAG)
        counter++
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

    private fun toRegistrationReadyFragment() {
        logDebug("toRegistrationReadyFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationShareYourDataFragmentDirections.toRegistrationReadyFragment(),
            viewModelScope
        )
    }
}