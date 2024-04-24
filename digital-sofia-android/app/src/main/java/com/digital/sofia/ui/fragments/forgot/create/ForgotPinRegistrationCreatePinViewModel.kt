package com.digital.sofia.ui.fragments.forgot.create

import androidx.lifecycle.viewModelScope
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digital.sofia.ui.fragments.base.registration.pin.BaseRegistrationCreatePinViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SupportBiometricManager
import com.digital.sofia.utils.UpdateDocumentsHelper

class ForgotPinRegistrationCreatePinViewModel(
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    preferences: PreferencesRepository,
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
) : BaseRegistrationCreatePinViewModel(
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
        private const val TAG = "ForgotPinRegistrationCreatePinViewModelTag"
    }

    override val checkPreviousPin = false

    override fun sendNewCodeRemote(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("sendNewCodeRemote decryptedPin: $decryptedPin\nhashedPin: $hashedPin", TAG)
        onSendNewCodeSuccess(
            hashedPin = hashedPin,
            decryptedPin = decryptedPin,
        )
    }

    override fun navigateNextWithBiometric() {
        logDebug("navigateNextWithBiometric", TAG)
        findFlowNavController().navigateInMainThread(
            ForgotPinRegistrationCreatePinFragmentDirections.toEnableBiometricFragment(),
            viewModelScope
        )
    }

    override fun navigateNextWithoutBiometric() {
        logDebug("navigateNextWithoutBiometric", TAG)
        findFlowNavController().navigateInMainThread(
            ForgotPinRegistrationCreatePinFragmentDirections.toConfirmIdentificationFragment(),
            viewModelScope
        )
    }
}