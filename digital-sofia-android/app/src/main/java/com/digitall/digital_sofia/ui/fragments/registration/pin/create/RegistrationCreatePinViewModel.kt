package com.digitall.digital_sofia.ui.fragments.registration.pin.create

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digitall.digital_sofia.ui.fragments.base.pin.create.BaseCreateCodeViewModel
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationCreatePinViewModel(
    private val currentContext: CurrentContext,
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
        private const val TAG = "RegistrationCreatePinViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

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

    override fun navigateNext() {
        logDebug("navigateNext", TAG)
        val isBiometricAvailable = SupportBiometricManager.hasBiometrics(currentContext.get())
        if (isBiometricAvailable) {
            logDebug("navigateNext isBiometricAvailable", TAG)
            findFlowNavController().navigateInMainThread(
                RegistrationCreatePinFragmentDirections.toRegistrationEnableBiometricFragment(),
                viewModelScope
            )
        } else {
            logDebug("navigateNext not isBiometricAvailable", TAG)
            findFlowNavController().navigateInMainThread(
                RegistrationCreatePinFragmentDirections.toRegistrationIntroFragment(),
                viewModelScope
            )
        }
    }
}