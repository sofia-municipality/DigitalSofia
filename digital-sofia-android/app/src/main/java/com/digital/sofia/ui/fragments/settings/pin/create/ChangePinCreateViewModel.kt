/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.pin.create

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.SdkStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.settings.ChangePinUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.fragments.base.pin.create.BaseCreatePinViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.EvrotrustSDKHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SupportBiometricManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChangePinCreateViewModel(
    private val preferences: PreferencesRepository,
    private val changePinUseCase: ChangePinUseCase,
    private val evrotrustSDKHelper: EvrotrustSDKHelper,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    biometricManager: SupportBiometricManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
    createCodeResponseErrorToStringMapper: CreateCodeResponseErrorToStringMapper,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
) : BaseCreatePinViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    biometricManager = biometricManager,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
    createCodeResponseErrorToStringMapper = createCodeResponseErrorToStringMapper,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
) {

    companion object {
        private const val TAG = "ChangePinCreateViewModelTag"
    }

    override val isAuthorizationActive: Boolean = true

    override val checkPreviousPin = true

    override fun sendNewCodeRemote(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("sendNewCodeRemote decryptedPin: $decryptedPin\nhashedPin: $hashedPin", TAG)
        changePinUseCase.invoke(
            newPin = hashedPin,
        ).onEach { result ->
            result.onLoading {
                logDebug("sendNewCodeRemote onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("sendNewCodeRemote onSuccess", TAG)
                viewModelScope.launch(Dispatchers.Main) {
                    evrotrustSDKHelper.changeSecurityContext(hashedPin)
                }
            }.onRetry {
                sendNewCodeRemote(hashedPin = hashedPin, decryptedPin = decryptedPin)
            }.onFailure {
                logError("sendNewCodeRemote onFailure", it, TAG)
                hideLoader()
                showMessage(Message.error(R.string.error_server_error))
                finishFlow()
            }
        }.launchInScope(viewModelScope)
    }


    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.SDK_SETUP_ERROR,
            SdkStatus.USER_SETUP_ERROR,
            SdkStatus.CRITICAL_ERROR -> {
                logError("onSdkStatusChanged error status", TAG)
                logout()
            }

            SdkStatus.CHANGE_SECURITY_CONTEXT_READY -> {
                if (hashedPin.isNullOrEmpty() || decryptedPin.isNullOrEmpty()) {
                    logError(
                        "onSdkStatusChanged hashedPin.isNullOrEmpty() || decryptedPin.isNullOrEmpty()",
                        TAG
                    )
                    logout()
                    return
                }
                onSendNewCodeSuccess(
                    hashedPin = hashedPin!!,
                    decryptedPin = decryptedPin!!,
                )
            }

            else -> {
                logError("onSdkStatusChanged status else", TAG)
                showMessage(Message.error(R.string.error_loading_error))
                returnOldPin()
            }
        }
    }

    private fun returnOldPin() {
        changePinUseCase.invoke(
            newPin = preferences.readPinCode()?.hashedPin!!,
        ).onEach { result ->
            result.onLoading {
                logDebug("returnOldPin onLoading", TAG)
            }.onSuccess {
                logDebug("returnOldPin onSuccess", TAG)
                hideLoader()
                showMessage(Message.error(R.string.error_server_error))
                finishFlow()
            }.onRetry {
                returnOldPin()
            }.onFailure {
                logError("returnOldPin onFailure", it, TAG)
                hideLoader()
                showMessage(Message.error(R.string.error_server_error))
                finishFlow()
            }
        }.launchInScope(viewModelScope)
    }

    override fun navigateNextWithBiometric() {
        logDebug("navigateNextWithBiometric", TAG)
        showMessage(Message.success(R.string.change_pin_create_message_ok))
        hideLoader()
        findFlowNavController().navigateInMainThread(
            ChangePinCreateFragmentDirections.toChangePinEnableBiometricFragment(),
            viewModelScope
        )
    }

    override fun navigateNextWithoutBiometric() {
        logDebug("navigateNextWithoutBiometric", TAG)
        showMessage(Message.success(R.string.change_pin_create_message_ok))
        hideLoader()
        finishFlow()
    }
}