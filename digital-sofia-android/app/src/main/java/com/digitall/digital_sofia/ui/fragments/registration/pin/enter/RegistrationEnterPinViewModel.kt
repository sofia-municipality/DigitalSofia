package com.digitall.digital_sofia.ui.fragments.registration.pin.enter

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.common.BiometricStatus
import com.digitall.digital_sofia.domain.models.common.ErrorStatus
import com.digitall.digital_sofia.domain.models.common.PinCode
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.usecase.registration.RegistrationUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.fragments.base.pin.enter.BaseEnterCodeViewModel
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.onEach

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationEnterPinViewModel(
    private val preferences: PreferencesRepository,
    private val registrationUseCase: RegistrationUseCase,
    logoutUseCase: LogoutUseCase,
    currentContext: CurrentContext,
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
        private const val TAG = "RegistrationEnterPinViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    override fun onForgotCodeClicked() {
        logDebug("onForgotCodeClicked", TAG)
    }

    override fun isBiometricAvailable(context: Context): Boolean {
        return false
    }

    override fun checkCode(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("checkCode decryptedPin: $decryptedPin hashedPin: $hashedPin", TAG)
        val user = preferences.readUser()
        if (user == null) {
            logError("checkCode user == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        val personalIdentificationNumber = user.personalIdentificationNumber
        if (personalIdentificationNumber.isNullOrEmpty()) {
            logError("checkCode user.personalIdentificationNumber.isNullOrEmpty()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        registrationUseCase.checkPin(
            hashedPin = hashedPin,
            personalIdentificationNumber = personalIdentificationNumber,
        ).onEach { result ->
            result.onLoading {
                logDebug("checkCode onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("checkCode onSuccess", TAG)
                hideLoader()
                if (it.matches == true) {
                    preferences.savePinCode(
                        PinCode(
                            errorCount = 3,
                            encryptedPin = null,
                            errorTimeCode = null,
                            hashedPin = hashedPin,
                            decryptedPin = decryptedPin,
                            errorStatus = ErrorStatus.NO_TIMEOUT,
                            biometricStatus = BiometricStatus.UNSPECIFIED,
                        )
                    )
                    navigateNext()
                } else {
                    resetCodeWhenNeeded()
                    showBannerMessage(BannerMessage.error(R.string.auth_enter_pin_error_check_remote))
                }
            }.onFailure {
                logError("checkCode onFailure", TAG)
                hideLoader()
                resetCodeWhenNeeded()
                showBannerMessage(BannerMessage.error(R.string.auth_enter_pin_error_check_remote))
            }
        }.launch(viewModelScope)
    }

    override fun navigateNext() {
        findFlowNavController().navigateInMainThread(
            RegistrationEnterPinFragmentDirections.toRegistrationIntroFragment(),
            viewModelScope
        )
    }

}