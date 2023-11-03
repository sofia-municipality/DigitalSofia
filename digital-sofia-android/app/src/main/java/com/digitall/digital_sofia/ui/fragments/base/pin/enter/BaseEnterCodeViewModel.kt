package com.digitall.digital_sofia.ui.fragments.base.pin.enter

import android.content.Context
import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.extensions.sha256
import com.digitall.digital_sofia.domain.models.common.BiometricStatus
import com.digitall.digital_sofia.domain.models.common.ErrorStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.models.common.AuthenticationCodeMethod
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.InputLockState
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.CodeKeyboardHelper
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseEnterCodeViewModel(
    private val currentContext: CurrentContext,
    private val preferences: PreferencesRepository,
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
        private const val TAG = "BaseEnterCodeViewModelTag"
    }

    protected open fun onCodeLocalCheckSuccess(hashedPin: String) {
        // CAN BE OVERRIDE IF NEED
    }

    abstract fun isBiometricAvailable(context: Context): Boolean

    abstract fun checkCode(
        hashedPin: String,
        decryptedPin: String,
    )

    abstract fun navigateNext()

    protected fun checkIsBiometricAvailable(context: Context): Boolean {
        val isBiometricAvailable = SupportBiometricManager.hasBiometrics(context) &&
                preferences.readPinCode()?.biometricStatus == BiometricStatus.BIOMETRIC
        _enableBiometricLiveData.value = isBiometricAvailable
        logDebug("checkIsBiometricAvailable isBiometricAvailable: $isBiometricAvailable", TAG)
        return isBiometricAvailable
    }

    open fun onForgotCodeClicked() {
        logDebug("onForgotCodeClicked", TAG)
        // TODO
    }

    private var authenticationCodeMethod = AuthenticationCodeMethod.IDLE

    private val codeKeyboardHelper = CodeKeyboardHelper {
        _inputLockStateLiveData.value = it
    }

    private val _inputLockStateLiveData = SingleLiveEvent<InputLockState>()
    val keyboardLockStateLiveData = _inputLockStateLiveData.readOnly()

    private val _clearInputLiveData = SingleLiveEvent<Unit>()
    val clearPassCodeFieldLiveData = _clearInputLiveData.readOnly()

    private val _shakeInputLiveData = SingleLiveEvent<Unit>()
    val shakePassCodeFieldLiveData = _shakeInputLiveData.readOnly()

    private val _startBiometricAuthLiveData = SingleLiveEvent<Cipher>()
    val startBiometricAuthLiveData = _startBiometricAuthLiveData.readOnly()

    private val _enableBiometricLiveData = MutableLiveData<Boolean>()
    val enableBiometricLiveData = _enableBiometricLiveData.readOnly()

    fun onBiometricException() {
        logError("onBiometricException", TAG)
        showBannerMessage(BannerMessage.error("Error use biometric"))
        _enableBiometricLiveData.value = false
    }

    fun onBiometricTooManyAttempts() {
        logError("onBiometricTooManyAttempts", TAG)
        showBannerMessage(BannerMessage.error(R.string.auth_biometric_scanner_many_attempts))
        _enableBiometricLiveData.value = false
    }

    fun onBiometricError() {
        logError("onBiometricError", TAG)
        showBannerMessage(BannerMessage.error(R.string.auth_biometric_scanner_failed))
        _enableBiometricLiveData.value = false
    }

    @CallSuper
    override fun onFirstAttach() {
        super.onFirstAttach()
        logDebug("onFirstAttach", TAG)
    }

    fun onPinCompleted(decryptedPin: String) {
        logDebug("onCodeCompleted decryptedPin: $decryptedPin", TAG)
        authenticationCodeMethod = AuthenticationCodeMethod.MANUAL
        val hashedPin = decryptedPin.sha256()
        if (hashedPin.isNullOrEmpty()) {
            logError("hashedPin.isNullOrEmpty", TAG)
            showBannerMessage(BannerMessage.error(R.string.error))
            finishFlow()
            return
        }
        checkCode(
            hashedPin = hashedPin,
            decryptedPin = decryptedPin,
        )
    }

    protected fun checkCodeLocal(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("checkCodeLocal decryptedPin: $decryptedPin\nhashedPin: $hashedPin ", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("checkCodeLocal pinCode == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("checkCodeLocal !pinCode.validate()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (pinCode.errorStatus != ErrorStatus.NO_TIMEOUT &&
            pinCode.errorCount == 3 &&
            pinCode.errorTimeCode != null &&
            pinCode.errorTimeCode != 0L
        ) {
            val timeOnUnlock = pinCode.errorTimeCode!! + pinCode.errorStatus.timeoutMillis
            if (System.currentTimeMillis() < timeOnUnlock) {
                val timeLeft = (timeOnUnlock - System.currentTimeMillis()) / 1000L
                logError("pin blocked, timeLeft: $timeLeft", TAG)
                resetCodeWhenNeeded()
                showBannerMessage(BannerMessage.error("Pin is blocked for ${pinCode.errorStatus.text}, try again later"))
                return
            }
        }
        if (pinCode.hashedPin == hashedPin && pinCode.decryptedPin == decryptedPin) {
            logDebug("checkCodeLocal pinCode.hashedPin == hashedPin", TAG)
            preferences.savePinCode(
                pinCode.copy(
                    errorCount = 3,
                    errorStatus = ErrorStatus.NO_TIMEOUT,
                    errorTimeCode = null,
                )
            )
            onCodeLocalCheckSuccess(
                hashedPin = hashedPin,
            )
            return
        }
        if (pinCode.errorCount > 1) {
            logDebug("pinCode.errorCount > 1", TAG)
            val newErrorCount = pinCode.errorCount - 1
            resetCodeWhenNeeded()
            preferences.savePinCode(
                pinCode.copy(
                    errorCount = newErrorCount,
                    errorTimeCode = System.currentTimeMillis(),
                )
            )
            showBannerMessage(
                BannerMessage.error(
                    currentContext.get().getString(
                        R.string.auth_enter_pin_incorrect,
                        newErrorCount.toString()
                    )
                )
            )
            return
        }
        logDebug("pinCode.errorCount == 0", TAG)
        when (pinCode.errorStatus) {
            ErrorStatus.NO_TIMEOUT -> {
                logDebug("checkCodeLocal timeout NO_TIMEOUT to TIMEOUT_30_SECONDS", TAG)
                resetCodeWhenNeeded()
                val newErrorStatus = ErrorStatus.TIMEOUT_30_SECONDS
                preferences.savePinCode(
                    pinCode.copy(
                        errorCount = 3,
                        errorStatus = newErrorStatus,
                        errorTimeCode = System.currentTimeMillis(),
                    )
                )
                showBannerMessage(BannerMessage.error("Pin is blocked for ${newErrorStatus.text}, try again later"))
            }

            ErrorStatus.TIMEOUT_30_SECONDS -> {
                logDebug("checkCodeLocal timeout TIMEOUT_30_SECONDS to TIMEOUT_5_MINUTES", TAG)
                resetCodeWhenNeeded()
                val newErrorStatus = ErrorStatus.TIMEOUT_5_MINUTES
                preferences.savePinCode(
                    pinCode.copy(
                        errorCount = 3,
                        errorStatus = ErrorStatus.TIMEOUT_5_MINUTES,
                        errorTimeCode = System.currentTimeMillis(),
                    )
                )
                showBannerMessage(BannerMessage.error("Pin is blocked for ${newErrorStatus.text}, try again later"))
            }

            ErrorStatus.TIMEOUT_5_MINUTES -> {
                logDebug("checkCodeLocal timeout TIMEOUT_5_MINUTES to TIMEOUT_1_HOUR", TAG)
                resetCodeWhenNeeded()
                val newErrorStatus = ErrorStatus.TIMEOUT_1_HOUR
                preferences.savePinCode(
                    pinCode.copy(
                        errorCount = 3,
                        errorStatus = ErrorStatus.TIMEOUT_1_HOUR,
                        errorTimeCode = System.currentTimeMillis(),
                    )
                )
                showBannerMessage(BannerMessage.error("Pin is blocked for ${newErrorStatus.text}, try again later"))
            }

            ErrorStatus.TIMEOUT_1_HOUR -> {
                logDebug("checkCodeLocal timeout TIMEOUT_1_HOUR to TIMEOUT_24_HOUR", TAG)
                resetCodeWhenNeeded()
                val newErrorStatus = ErrorStatus.TIMEOUT_24_HOUR
                preferences.savePinCode(
                    pinCode.copy(
                        errorCount = 3,
                        errorStatus = ErrorStatus.TIMEOUT_24_HOUR,
                        errorTimeCode = System.currentTimeMillis(),
                    )
                )
                showBannerMessage(BannerMessage.error("Pin is blocked for ${newErrorStatus.text}, try again later"))
            }

            ErrorStatus.TIMEOUT_24_HOUR -> {
                logDebug("checkCodeLocal timeout TIMEOUT_24_HOUR to TIMEOUT_24_HOUR", TAG)
                resetCodeWhenNeeded()
                val newErrorStatus = ErrorStatus.TIMEOUT_24_HOUR
                preferences.savePinCode(
                    pinCode.copy(
                        errorCount = 3,
                        errorStatus = ErrorStatus.TIMEOUT_24_HOUR,
                        errorTimeCode = System.currentTimeMillis(),
                    )
                )
                showBannerMessage(BannerMessage.error("Pin is blocked for ${newErrorStatus.text}, try again later"))
            }

            else -> {
                logDebug("checkCodeLocal timeout else to TIMEOUT_24_HOUR", TAG)
                resetCodeWhenNeeded()
                val newErrorStatus = ErrorStatus.TIMEOUT_24_HOUR
                preferences.savePinCode(
                    pinCode.copy(
                        errorCount = 3,
                        errorStatus = ErrorStatus.TIMEOUT_24_HOUR,
                        errorTimeCode = System.currentTimeMillis(),
                    )
                )
                showBannerMessage(BannerMessage.error("Pin is blocked for ${newErrorStatus.text}, try again later"))
            }
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
            logError("startBiometricAuth !pinCode.validateWithEncrypted()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            disableBiometric()
            _enableBiometricLiveData.value = false
            return
        }
        val cipher = getBiometricCipherForDecryption()
        if (cipher == null) {
            logError("startBiometricAuth cipher == null", TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            _enableBiometricLiveData.value = false
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
            _enableBiometricLiveData.value = false
            return
        }
        if (cipher == null) {
            logError("onBiometricSuccess cipher == null", TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            _enableBiometricLiveData.value = false
            return
        }
        try {
            val decryptedPin = cryptographyRepository.decrypt(pinCode.encryptedPin!!, cipher)
            if (decryptedPin.isEmpty()) {
                logError("onBiometricSuccess decryptedCode.isEmpty", TAG)
                showBannerMessage(BannerMessage.error("Error use biometric"))
                _enableBiometricLiveData.value = false
                return
            }
            if (decryptedPin != pinCode.decryptedPin) {
                logError("onBiometricSuccess decryptedPin != pinCode.decryptedPin", TAG)
                showBannerMessage(BannerMessage.error("Error use biometric"))
                _enableBiometricLiveData.value = false
                return
            }
            authenticationCodeMethod = AuthenticationCodeMethod.BIOMETRIC
            _clearInputLiveData.call()
            preferences.savePinCode(
                pinCode.copy(
                    errorCount = 3,
                    errorStatus = ErrorStatus.NO_TIMEOUT,
                    errorTimeCode = null,
                )
            )
            onCodeLocalCheckSuccess(
                hashedPin = pinCode.hashedPin!!,
            )
        } catch (e: Exception) {
            logError(e, TAG)
            showBannerMessage(BannerMessage.error("Error use biometric"))
            _enableBiometricLiveData.value = false
        }
    }

    fun completeAuthentication() {
        authenticationCodeMethod = AuthenticationCodeMethod.IDLE
        _clearInputLiveData.call()
    }

    protected fun resetCodeWhenNeeded() {
        if (authenticationCodeMethod == AuthenticationCodeMethod.MANUAL) {
            resetCodeAndShakeInputField()
        }
    }

    private fun resetCodeAndShakeInputField() {
        logDebug("resetCodeAndShakeInputField", TAG)
        if (isMainThread()) {
            _clearInputLiveData.call()
            codeKeyboardHelper.lockKeyboardForTimeout(TimeUnit.SECONDS.toMillis(1L))
            _shakeInputLiveData.call()
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _clearInputLiveData.call()
                codeKeyboardHelper.lockKeyboardForTimeout(TimeUnit.SECONDS.toMillis(1L))
                _shakeInputLiveData.call()
            }
        }
    }

}