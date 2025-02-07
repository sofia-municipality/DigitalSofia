/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.confirmation.confirmation

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.confirmation.ConfirmationCodeStatusModel
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.confirmation.ConfirmationGetCodeStatusUseCase
import com.digital.sofia.domain.usecase.confirmation.ConfirmationUpdateCodeStatusUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import kotlinx.coroutines.flow.onEach

class ConfirmationViewModel(
    private val confirmationGetCodeStatusUseCase: ConfirmationGetCodeStatusUseCase,
    private val confirmationUpdateCodeStatusUseCase: ConfirmationUpdateCodeStatusUseCase,
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
        private const val TAG = "ConfirmationViewModelTag"
        private const val CODE_STATUS_CONFIRMED = "confirmed"
        private const val CODE_STATUS_CANCELLED = "cancelled"
    }

    override val isAuthorizationActive: Boolean = true

    override val isAuthorizationWithAppUpdateEnabled = false

    private var confirmationModel: ConfirmationCodeStatusModel? = null

    override fun onFirstAttach() {
        getCodeStatus()
    }

    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        onNoClicked()
    }

    private fun getCodeStatus() {
        logDebug("getCodeStatus", TAG)
        confirmationGetCodeStatusUseCase.invoke().onEach { result ->
            result.onLoading {
                logDebug("getCodeStatus onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("getCodeStatus onSuccess", TAG)
                if (it.codeExists != true || it.code.isNullOrEmpty()) {
                    logError("getCodeStatus codeExists false", TAG)
                    showMessage(Message.error(R.string.error_information_is_outdated))
                    finishFlow()
                } else {
                    confirmationModel = it
                    hideLoader()
                }
            }.onRetry {
                getCodeStatus()
            }.onFailure {
                logError("getCodeStatus onFailure", it, TAG)
                hideLoader()
                showMessage(Message.error(R.string.error_server_error))
            }
        }.launchInScope(viewModelScope)
    }

    fun onYesClicked() {
        logDebug("onYesClicked", TAG)
        if (confirmationModel == null) {
            logError("confirmationModel == null", TAG)
            showMessage(Message.error(R.string.error))
            finishFlow()
            return
        }
        logDebug("onYesClicked code: ${confirmationModel?.code}", TAG)
        confirmationUpdateCodeStatusUseCase.updateCodeStatus(
            code = confirmationModel?.code ?: "",
            status = CODE_STATUS_CONFIRMED,
        ).onEach { result ->
            result.onLoading {
                logDebug("onYesClicked onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("onYesClicked onSuccess", TAG)
                if (it.codeUpdated == true) {
                    showMessage(Message.success(R.string.ready))
                } else {
                    logError("onYesClicked isCodeUpdated != true", TAG)
                    showMessage(Message.error(R.string.error_information_is_outdated))
                }
                finishFlow()
            }.onRetry {
                onYesClicked()
            }.onFailure {
                logError("onYesClicked onFailure", it, TAG)
                showMessage(Message.error(R.string.error_server_error))
                finishFlow()
            }
        }.launchInScope(viewModelScope)
    }

    fun onNoClicked() {
        logDebug("onNoClicked", TAG)
        if (confirmationModel == null) {
            logError("confirmationModel == null", TAG)
            showMessage(Message.error(R.string.error))
            finishFlow()
            return
        }
        logDebug("onNoClicked code: ${confirmationModel?.code}", TAG)
        confirmationUpdateCodeStatusUseCase.updateCodeStatus(
            code = confirmationModel?.code ?: "",
            status = CODE_STATUS_CANCELLED,
        ).onEach { result ->
            result.onLoading {
                logDebug("onNoClicked onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("onNoClicked onSuccess", TAG)
                if (it.codeUpdated == true) {
                    showMessage(Message.success(R.string.ready))
                } else {
                    logError("onNoClicked isCodeUpdated != true", TAG)
                    showMessage(Message.error(R.string.error_information_is_outdated))
                }
                finishFlow()
            }.onRetry {
               onNoClicked()
            }.onFailure {
                logError("onNoClicked onFailure", it, TAG)
                showMessage(Message.error(R.string.error_server_error))
                finishFlow()
            }
        }.launchInScope(viewModelScope)
    }

}