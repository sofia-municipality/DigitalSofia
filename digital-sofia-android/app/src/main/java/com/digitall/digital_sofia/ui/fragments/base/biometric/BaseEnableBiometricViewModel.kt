package com.digitall.digital_sofia.ui.fragments.base.biometric

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import javax.crypto.Cipher

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseEnableBiometricViewModel(
    private val preferences: PreferencesRepository,
    private val biometricManager: SupportBiometricManager,
    private val cryptographyRepository: CryptographyRepository,
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
        private const val TAG = "BaseEnableBiometricViewModelTag"
    }

    abstract fun navigateNext()

    fun startBiometric() {
        logDebug("startBiometric", TAG)
        val cipher = cryptographyRepository.getBiometricCipherForEncryption()
        if (cipher == null) {
            logError("startBiometric cipher == null", TAG)
            findActivityNavController().navigateInMainThread(
                NavActivityDirections.toBiometricErrorBottomSheetFragment(),
                viewModelScope
            )
            return
        }
        try {
            biometricManager.authenticate(cipher)
        } catch (e: Exception) {
            logError("handleStartBiometricAuth exception: ${e.message}", e, TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            setupLater()
        }
    }

    fun setupLater() {
        logDebug("setupLater", TAG)
        disableBiometric()
        navigateNext()
    }

    fun onBiometricTooManyAttempts() {
        logError("onBiometricTooManyAttempts", TAG)
        showBannerMessage(BannerMessage.error(R.string.auth_biometric_scanner_many_attempts))
        setupLater()
    }

    fun onBiometricError() {
        logError("onBiometricError", TAG)
        showBannerMessage(BannerMessage.error(R.string.auth_biometric_scanner_failed))
        setupLater()
    }

    fun onBiometricSuccess(cipher: Cipher?) {
        logDebug("confirmFingerprint", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("confirmFingerprint pinCode == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("confirmFingerprint !pinCode.validate()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (cipher == null) {
            logError("confirmFingerprint cipher == null", TAG)
            showBannerMessage(BannerMessage.error("Error enable biometric"))
            disableBiometric()
            navigateNext()
            return
        }
        try {
            val encryptedPin = cryptographyRepository.encrypt(pinCode.decryptedPin!!, cipher)
            logDebug(
                "confirmFingerprint pinCode.hashedPin: ${pinCode.hashedPin}\n" +
                        "pinCode.decryptedPin: ${pinCode.decryptedPin}\n" +
                        "pinCode.encryptedPin: ${pinCode.encryptedPin}\n" +
                        "encryptedPin: $encryptedPin", TAG
            )
            if (encryptedPin.isEmpty()) {
                logError("confirmFingerprint encryptedPin.isEmpty()", TAG)
                showBannerMessage(BannerMessage.error("Error enable biometric"))
                disableBiometric()
                navigateNext()
                return
            }
            enableBiometric(encryptedPin)
            navigateNext()
        } catch (e: Exception) {
            logError(e, TAG)
            showBannerMessage(BannerMessage.error("Error enable biometric"))
            disableBiometric()
            navigateNext()
        }
    }

    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        disableBiometric()
        navigateNext()
    }

}