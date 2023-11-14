package com.digitall.digital_sofia.ui.fragments.settings.pin.enter

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.ui.fragments.base.pin.enter.BaseEnterCodeViewModel
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ChangePinEnterViewModel(
    logoutUseCase: LogoutUseCase,
    currentContext: CurrentContext,
    preferences: PreferencesRepository,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
) : BaseEnterCodeViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    currentContext = currentContext,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
) {

    companion object {
        private const val TAG = "ChangePinEnterViewModelTag"
    }

    override val needUpdateDocuments: Boolean = true

    override fun onCodeLocalCheckSuccess(hashedPin: String) {
        logDebug("onCodeLocalCheckSuccess hashedPin: $hashedPin", TAG)
        // TODO need check remote
        navigateNext()
    }

    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        finishFlow()
    }

    override fun isBiometricAvailable(context: Context): Boolean {
        return checkIsBiometricAvailable(context)
    }

    override fun checkCode(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("checkCode decryptedPin: $decryptedPin\nhashedPin: $hashedPin", TAG)
        checkCodeLocal(
            hashedPin = hashedPin,
            decryptedPin = decryptedPin,
        )
    }

    override fun navigateNext() {
        findFlowNavController().navigateInMainThread(
            ChangePinEnterFragmentDirections.toChangePinCreateFragment(), viewModelScope
        )
    }
}