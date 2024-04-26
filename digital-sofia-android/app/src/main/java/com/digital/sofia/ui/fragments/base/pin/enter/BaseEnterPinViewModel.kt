/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.pin.enter

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.extensions.sha256
import com.digital.sofia.domain.models.common.ErrorStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.AuthenticationCodeMethod
import com.digital.sofia.models.common.InputLockState
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CodeKeyboardHelper
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SingleLiveEvent
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher

abstract class BaseEnterPinViewModel(
    private val currentContext: CurrentContext,
    private val preferences: PreferencesRepository,
    private val cryptographyRepository: CryptographyRepository,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
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
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "BaseEnterPinViewModelTag"
    }

    protected open fun onCodeLocalCheckSuccess(hashedPin: String) {
        // CAN BE OVERRIDE IF NEED
    }

    abstract fun isBiometricAvailable(): Boolean

    abstract fun checkCode(
        hashedPin: String,
        decryptedPin: String,
    )

    abstract fun navigateNext()

    private var authenticationCodeMethod = AuthenticationCodeMethod.IDLE

    private val codeKeyboardHelper = CodeKeyboardHelper {
        _inputLockStateLiveData.setValueOnMainThread(it)
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
        showMessage(Message.error(R.string.error_biometric))
        _enableBiometricLiveData.setValueOnMainThread(false)
    }

    fun onBiometricTooManyAttempts() {
        logError("onBiometricTooManyAttempts", TAG)
        showMessage(Message.error(R.string.auth_biometric_scanner_many_attempts))
        _enableBiometricLiveData.setValueOnMainThread(false)
    }

    fun onBiometricError() {
        logError("onBiometricError", TAG)
        showMessage(Message.error(R.string.auth_biometric_scanner_failed))
        _enableBiometricLiveData.setValueOnMainThread(false)
    }

    @CallSuper
    override fun onFirstAttach() {
        super.onFirstAttach()
        _enableBiometricLiveData.setValueOnMainThread(isBiometricAvailable())
    }

    fun onPinCompleted(decryptedPin: String) {
        logDebug("onCodeCompleted decryptedPin: $decryptedPin", TAG)
        authenticationCodeMethod = AuthenticationCodeMethod.MANUAL
        val hashedPin = decryptedPin.sha256()
        if (hashedPin.isNullOrEmpty()) {
            logError("hashedPin.isNullOrEmpty", TAG)
            showMessage(Message.error(R.string.error))
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
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("checkCodeLocal !pinCode.validate()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
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
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.information),
                        message = StringSource.Res(getTimeoutTextRes(pinCode.errorStatus)),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                    )
                )
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
            showMessage(
                Message(
                    title = StringSource.Res(R.string.information),
                    message = StringSource.Text(
                        currentContext.get().getString(
                            R.string.auth_enter_pin_incorrect,
                            newErrorCount.toString()
                        )
                    ),
                    type = Message.Type.ALERT,
                    positiveButtonText = StringSource.Res(R.string.ok),
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
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.information),
                        message = StringSource.Res(getTimeoutTextRes(newErrorStatus)),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                    )
                )
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
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.information),
                        message = StringSource.Res(getTimeoutTextRes(newErrorStatus)),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                    )
                )
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
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.information),
                        message = StringSource.Res(getTimeoutTextRes(newErrorStatus)),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                    )
                )
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
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.information),
                        message = StringSource.Res(getTimeoutTextRes(newErrorStatus)),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                    )
                )
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
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.information),
                        message = StringSource.Res(getTimeoutTextRes(newErrorStatus)),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                    )
                )
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
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.information),
                        message = StringSource.Res(getTimeoutTextRes(newErrorStatus)),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                    )
                )
            }
        }
    }

    fun startBiometricAuth() {
        logDebug("startBiometricAuth", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("startBiometricAuth pinCode == null", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("startBiometricAuth !pinCode.validate()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validateWithEncrypted()) {
            logError("startBiometricAuth !pinCode.validateWithEncrypted()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            disableBiometric()
            _enableBiometricLiveData.setValueOnMainThread(false)
            return
        }
        val cipher = getBiometricCipherForDecryption()
        if (cipher == null) {
            logError("startBiometricAuth cipher == null", TAG)
            showMessage(Message.error(R.string.error_biometric))
            _enableBiometricLiveData.setValueOnMainThread(false)
            return
        }
        logDebug("startBiometricAuth set live data", TAG)
        _startBiometricAuthLiveData.setValueOnMainThread(cipher)
    }

    fun onBiometricSuccess(cipher: Cipher?) {
        logDebug("onBiometricSuccess", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("onBiometricSuccess pinCode == null", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("onBiometricSuccess !pinCode.validate()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validateWithEncrypted()) {
            logError("onBiometricSuccess !pinCode.validateWithEncrypted()", TAG)
            showMessage(Message.error(R.string.error_biometric))
            disableBiometric()
            _enableBiometricLiveData.setValueOnMainThread(false)
            return
        }
        if (cipher == null) {
            logError("onBiometricSuccess cipher == null", TAG)
            showMessage(Message.error(R.string.error_biometric))
            _enableBiometricLiveData.setValueOnMainThread(false)
            return
        }
        try {
            val decryptedPin = cryptographyRepository.decrypt(pinCode.encryptedPin!!, cipher)
            if (decryptedPin.isEmpty()) {
                logError("onBiometricSuccess decryptedCode.isEmpty", TAG)
                showMessage(Message.error(R.string.error_biometric))
                _enableBiometricLiveData.setValueOnMainThread(false)
                return
            }
            if (decryptedPin != pinCode.decryptedPin) {
                logError("onBiometricSuccess decryptedPin != pinCode.decryptedPin", TAG)
                showMessage(Message.error(R.string.error_biometric))
                _enableBiometricLiveData.setValueOnMainThread(false)
                return
            }
            authenticationCodeMethod = AuthenticationCodeMethod.BIOMETRIC
            _clearInputLiveData.callOnMainThread()
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
            showMessage(Message.error(R.string.error_biometric))
            _enableBiometricLiveData.setValueOnMainThread(false)
        }
    }

    fun completeAuthentication() {
        authenticationCodeMethod = AuthenticationCodeMethod.IDLE
        _clearInputLiveData.callOnMainThread()
    }

    protected fun resetCodeWhenNeeded() {
        if (authenticationCodeMethod == AuthenticationCodeMethod.MANUAL) {
            resetCodeAndShakeInputField()
        }
    }

    private fun resetCodeAndShakeInputField() {
        logDebug("resetCodeAndShakeInputField", TAG)
        _clearInputLiveData.callOnMainThread()
        if (isMainThread()) {
            codeKeyboardHelper.lockKeyboardForTimeout(TimeUnit.SECONDS.toMillis(1L))
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                codeKeyboardHelper.lockKeyboardForTimeout(TimeUnit.SECONDS.toMillis(1L))
            }
        }
        _shakeInputLiveData.callOnMainThread()
    }

    private fun getTimeoutTextRes(errorStatus: ErrorStatus): Int {
        return when (errorStatus) {
            ErrorStatus.NO_TIMEOUT -> R.string.unknown
            ErrorStatus.TIMEOUT_30_SECONDS -> R.string.auth_enter_pin_incorrect_wait_30_seconds
            ErrorStatus.TIMEOUT_5_MINUTES -> R.string.auth_enter_pin_incorrect_wait_5_minutes
            ErrorStatus.TIMEOUT_1_HOUR -> R.string.auth_enter_pin_incorrect_wait_1_hour
            ErrorStatus.TIMEOUT_24_HOUR -> R.string.auth_enter_pin_incorrect_wait_24_hour
        }
    }

}