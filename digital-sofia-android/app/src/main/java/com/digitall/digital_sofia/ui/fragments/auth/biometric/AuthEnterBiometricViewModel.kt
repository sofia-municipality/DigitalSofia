package com.digitall.digital_sofia.ui.fragments.auth.biometric

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.extensions.capitalized
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.common.AppLanguage
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.authorization.AuthorizationUseCase
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.navigateNewRootInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.crypto.Cipher

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class AuthEnterBiometricViewModel(
    private val preferences: PreferencesRepository,
    private val cryptographyRepository: CryptographyRepository,
    private val authorizationUseCase: AuthorizationUseCase,
    logoutUseCase: LogoutUseCase,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
) : BaseViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
) {

    companion object {
        private const val TAG = "EnterBiometricViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    private val _userNameLiveData = MutableLiveData<String>()
    val userNameLiveData = _userNameLiveData.readOnly()

    private val _startBiometricAuthLiveData = SingleLiveEvent<Cipher>()
    val startBiometricAuthLiveData = _startBiometricAuthLiveData.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        val user = preferences.readUser()
        _userNameLiveData.value = when (preferences.readCurrentLanguage()) {
            AppLanguage.BG -> user?.firstName?.capitalized() ?: "потребител"
            AppLanguage.EN -> user?.firstLatinName?.capitalized() ?: "user"
        }
    }

    fun startBiometricAuth() {
        logDebug("startBiometricAuth", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("startBiometricAuth pinCode == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("startBiometricAuth !pinCode.validate()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validateWithEncrypted()) {
            disableBiometric()
            logError("startBiometricAuth !pinCode.validateWithEncrypted()", TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            toEnterPinFragment(true)
            return
        }
        val cipher = getBiometricCipherForDecryption()
        if (cipher == null) {
            logError("startBiometricAuth cipher == null", TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            toEnterPinFragment(true)
            return
        }
        _startBiometricAuthLiveData.value = cipher
    }

    fun onBiometricSuccess(cipher: Cipher?) {
        logDebug("onBiometricSuccess", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("onBiometricSuccess pinCode == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("onBiometricSuccess !pinCode.validate()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validateWithEncrypted()) {
            logError("onBiometricSuccess !pinCode.validateWithEncrypted()", TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            disableBiometric()
            toEnterPinFragment(true)
            return
        }
        if (cipher == null) {
            logError("onBiometricSuccess cipher == null", TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            toEnterPinFragment(true)
            return
        }
        try {
            val decryptedPin = cryptographyRepository.decrypt(pinCode.encryptedPin!!, cipher)
            if (decryptedPin.isEmpty()) {
                logError("onBiometricSuccess decryptedPin.isEmpty()", TAG)
                showBannerMessage(BannerMessage.error("Error use biometric"))
                toEnterPinFragment(true)
                return
            }
            if (decryptedPin != pinCode.decryptedPin) {
                logError("onBiometricSuccess decryptedPin != pinCode.decryptedPin", TAG)
                showBannerMessage(BannerMessage.error("Error use biometric"))
                toEnterPinFragment(true)
                return
            }
            onCodeLocalCheckSuccess(
                hashedPin = pinCode.hashedPin!!
            )
        } catch (e: Exception) {
            logError(e, TAG)
            showBannerMessage(BannerMessage.error("Error enable biometric"))
            toEnterPinFragment(true)
        }
    }

    private fun onCodeLocalCheckSuccess(hashedPin: String) {
        logDebug("onCodeLocalCheckSuccess hashedPin: $hashedPin", TAG)
        val user = preferences.readUser()
        if (user == null) {
            logError("onCodeLocalCheckSuccess user == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        if (!user.validate()) {
            logError("onCodeLocalCheckSuccess !user.validate())", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        val personalIdentificationNumber = user.personalIdentificationNumber
        if (personalIdentificationNumber.isNullOrEmpty()) {
            logError("onCodeLocalCheckSuccess personalIdentificationNumber.isNullOrEmpty", TAG)
            showBannerMessage(BannerMessage.error(R.string.error))
            logout()
            return
        }
        authorizationUseCase.enterToAccount(
            hashedPin = hashedPin,
            personalIdentificationNumber = personalIdentificationNumber,
        ).onEach { result ->
            result.onLoading {
                logDebug("onCodeLocalCheckSuccess onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("onCodeLocalCheckSuccess onSuccess", TAG)
                navigateNext()
            }.onFailure {
                logError("onCodeLocalCheckSuccess onFailure", TAG)
                showBannerMessage(BannerMessage.error(R.string.auth_enter_pin_error_check_remote))
                toEnterPinFragment(true)
            }
        }.launch(viewModelScope)
    }

    fun onBiometricTooManyAttempts() {
        logError("onBiometricTooManyAttempts", TAG)
        showBannerMessage(BannerMessage.error(R.string.auth_biometric_scanner_many_attempts))
        toEnterPinFragment(true)
    }

    fun onBiometricError() {
        logError("onBiometricError", TAG)
        showBannerMessage(BannerMessage.error(R.string.auth_biometric_scanner_failed))
        toEnterPinFragment(true)
    }

    fun onBiometricException() {
        logError("onBiometricException", TAG)
        showBannerMessage(BannerMessage.error("Error use biometric"))
        toEnterPinFragment(true)
    }

    private fun navigateNext() {
        logDebug("toMainTabs", TAG)
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toMainTabsFlowFragment(), viewModelScope
        )
        viewModelScope.launch {
            delay(1000)
            hideLoader()
        }
    }

    fun toEnterPinFragment(forceDisableBiometric: Boolean) {
        logDebug("toEnterPinFragment forceDisableBiometric: $forceDisableBiometric", TAG)
        findFlowNavController().navigateInMainThread(
            AuthEnterBiometricFragmentDirections.toEnterPinFragment(
                forceDisableBiometric = forceDisableBiometric
            ), viewModelScope
        )
        viewModelScope.launch {
            delay(1000)
            hideLoader()
        }
    }
}