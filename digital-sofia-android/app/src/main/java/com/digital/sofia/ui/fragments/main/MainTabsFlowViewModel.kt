/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.confirmation.ConfirmationGetCodeStatusUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsHaveUnsignedUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SingleLiveEvent
import kotlinx.coroutines.flow.onEach

class MainTabsFlowViewModel(
    private val documentsHaveUnsignedUseCase: DocumentsHaveUnsignedUseCase,
    private val confirmationGetCodeStatusUseCase: ConfirmationGetCodeStatusUseCase,
    private val appEventsHelper: AppEventsHelper,
    private val authorizationHelper: AuthorizationHelper,
    private val preferences: PreferencesRepository,
    loginTimer: LoginTimer,
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
        private const val TAG = "MainTabsFlowViewModelTag"
    }

    override val isAuthorizationActive: Boolean = true

    private val _newSignedDocumentLiveEventNotification = SingleLiveEvent<Unit>()
    val newSignedDocumentLiveEventNotification = _newSignedDocumentLiveEventNotification.readOnly()

    private val _newPendingDocumentLiveEventNotification = SingleLiveEvent<Unit>()
    val newPendingDocumentLiveEventNotification =
        _newPendingDocumentLiveEventNotification.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        checkForUnsignedDocuments()
        checkForLoginWebAttempts()
        onNewTokenEvent()
        startTimers()
    }

    private fun startTimers() {
        val accessTokenExpirationTime = preferences.readAccessToken()?.expirationTime
        authorizationHelper.startUpdateAccessTokenTimer(
            accessTokenExpiresIn = accessTokenExpirationTime ?: 0L
        )
    }

    private fun checkForUnsignedDocuments() {
        logDebug("checkForUnsignedDocuments", TAG)
        documentsHaveUnsignedUseCase.invoke(status = "signing,delivering").onEach { result ->
            result.onLoading {
                logDebug("checkForUnsignedDocuments onLoading", TAG)
            }.onSuccess {
                if (it.documents.isEmpty()) {
                    logDebug("checkForUnsignedDocuments isEmpty", TAG)
                    setHasNewUnsignedDocuments(false)
                } else {
                    logDebug("checkForUnsignedDocuments isEmpty", TAG)
                    setHasNewUnsignedDocuments(true)
                }
            }.onRetry {
                checkForUnsignedDocuments()
            }.onFailure {
                logError("checkForUnsignedDocuments onFailure", it, TAG)
                showMessage(Message.error(R.string.error_server_error))
            }
        }.launchInScope(viewModelScope)
    }

    private fun checkForLoginWebAttempts() {
        logDebug("checkForLoginWebAttempts", TAG)
        confirmationGetCodeStatusUseCase.invoke().onEach { result ->
            result.onLoading {
                logDebug("checkForLoginWebAttempts onLoading", TAG)
            }.onSuccess {
                logDebug("checkForLoginWebAttempts onSuccess", TAG)
                if (it.codeExists == true && !appEventsHelper.hasNewAuthorizationEvent) {
                    onNewAuthorizationEvent()
                }
            }.onRetry {
                checkForLoginWebAttempts()
            }.onFailure {
                logError("checkForLoginWebAttempts onFailure", it, TAG)
                showMessage(Message.error(R.string.error_server_error))
            }
        }.launchInScope(viewModelScope)
    }

    override fun onNewSignedDocumentEvent(isNotificationEvent: Boolean) {
        _newSignedDocumentLiveEventNotification.callOnMainThread()
        checkForUnsignedDocuments()
    }

    override fun onNewPendingDocumentEvent(isNotificationEvent: Boolean) {
        _newPendingDocumentLiveEventNotification.callOnMainThread()
        setHasNewUnsignedDocuments(true)
    }
}