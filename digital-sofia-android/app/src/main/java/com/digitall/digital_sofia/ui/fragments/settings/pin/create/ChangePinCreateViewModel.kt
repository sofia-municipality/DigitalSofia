package com.digitall.digital_sofia.ui.fragments.settings.pin.create

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.domain.models.common.SdkStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digitall.digital_sofia.ui.fragments.base.pin.create.BaseCreateCodeViewModel
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ChangePinCreateViewModel(
    private val currentContext: CurrentContext,
    private val evrotrustSDKHelper: EvrotrustSDKHelper,
    logoutUseCase: LogoutUseCase,
    preferences: PreferencesRepository,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
    createCodeResponseErrorToStringMapper: CreateCodeResponseErrorToStringMapper,
) : BaseCreateCodeViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
    createCodeResponseErrorToStringMapper = createCodeResponseErrorToStringMapper,
) {

    companion object {
        private const val TAG = "ChangePinCreateViewModelTag"
    }

    override val needUpdateDocuments: Boolean = true

    override fun sendNewCodeRemote(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("sendNewCodeRemote decryptedPin: $decryptedPin\nhashedPin: $hashedPin", TAG)
        evrotrustSDKHelper.changeSecurityContext(hashedPin)
        // TODO
    }

    override fun navigateNext() {
        logDebug("navigateNext", TAG)
        val isBiometricAvailable = SupportBiometricManager.hasBiometrics(currentContext.get())
        if (isBiometricAvailable) {
            logDebug("navigateNext isBiometricAvailable", TAG)
            findFlowNavController().navigateInMainThread(
                ChangePinCreateFragmentDirections.toChangePinEnableBiometricFragment(),
                viewModelScope
            )
        } else {
            logDebug("navigateNext not isBiometricAvailable", TAG)
            finishFlow()
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

            SdkStatus.CHANGE_SECURITY_CONTEXT_READY -> {
                if (hashedPin.isNullOrEmpty() || decryptedPin.isNullOrEmpty()) {
                    logError("onSdkStatusChanged hashedPin.isNullOrEmpty() || decryptedPin.isNullOrEmpty()", TAG)
                    logout()
                    return
                }
                onSendNewCodeSuccess(
                    hashedPin = hashedPin!!,
                    decryptedPin = decryptedPin!!,
                )
            }

            else -> {
                finishFlow()
            }
        }
    }
}