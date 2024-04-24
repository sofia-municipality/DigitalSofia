/**
 * Support class to interact with biometric sensors.
 * Should be injected in fragment and setup in fragment initialization step.
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.digital.sofia.R
import com.digital.sofia.domain.models.common.BiometricStatus
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import javax.crypto.Cipher

interface SupportBiometricManager {

    fun hasBiometrics(): Boolean

    fun readyToBiometricAuth(): Boolean

    val onBiometricErrorLiveData: LiveData<Unit>

    val onBiometricTooManyAttemptsLiveData: LiveData<Unit>

    val onBiometricSuccessLiveData: LiveData<Cipher?>

    fun setupBiometricManager(activity: FragmentActivity)

    fun authenticate(cipher: Cipher?)

    fun cancelAuthentication()
}

class SupportBiometricManagerImpl(
    private val currentContext: CurrentContext,
    private val preferences: PreferencesRepository,
    private val mainThreadExecutor: MainThreadExecutor,
) : SupportBiometricManager {

    companion object {
        private const val TAG = "SupportBiometricManagerTag"
    }

    private val _onBiometricErrorLiveData = SingleLiveEvent<Unit>()
    override val onBiometricErrorLiveData = _onBiometricErrorLiveData.readOnly()

    private val _onBiometricTooManyAttemptsLiveData = SingleLiveEvent<Unit>()
    override val onBiometricTooManyAttemptsLiveData = _onBiometricTooManyAttemptsLiveData.readOnly()

    private val _onBiometricSuccessLiveData = SingleLiveEvent<Cipher?>()
    override val onBiometricSuccessLiveData = _onBiometricSuccessLiveData.readOnly()

    override fun hasBiometrics(): Boolean {
        return BiometricManager.from(currentContext.get()).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun readyToBiometricAuth(): Boolean {
        return hasBiometrics() && preferences.readPinCode()?.biometricStatus == BiometricStatus.BIOMETRIC
    }

    private var biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(currentContext.getString(R.string.auth_biometric_scanner_title))
        .setDescription(currentContext.getString(R.string.auth_biometric_scanner_description))
        .setNegativeButtonText(currentContext.getString(R.string.cancel))
        .build()

    private var biometricPrompt: BiometricPrompt? = null

    private val promptCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            logError("onAuthenticationError", TAG)
            when {
                errorCode == BiometricPrompt.ERROR_LOCKOUT ||
                        errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                    _onBiometricErrorLiveData.callOnMainThread()
                }

                errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED -> {
                    _onBiometricErrorLiveData.callOnMainThread()
                }
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            logDebug("onAuthenticationSucceeded", TAG)
            val cipher = result.cryptoObject?.cipher
            _onBiometricSuccessLiveData.setValueOnMainThread(cipher)
        }

        override fun onAuthenticationFailed() {
            logError("onAuthenticationFailed", TAG)
        }
    }

    override fun setupBiometricManager(activity: FragmentActivity) {
        logDebug("setupBiometricManager", TAG)
        biometricPrompt?.cancelAuthentication()
        biometricPrompt = BiometricPrompt(activity, mainThreadExecutor, promptCallback)
    }

    override fun authenticate(cipher: Cipher?) {
        if (biometricPrompt == null) {
            logError("authenticate biometricPrompt == null", TAG)
            return
        }
        if (cipher == null) {
            logError("authenticate cipher == null", TAG)
            biometricPrompt?.authenticate(biometricPromptInfo)
        } else {
            logDebug("authenticate cipher != null", TAG)
            biometricPrompt?.authenticate(
                biometricPromptInfo,
                BiometricPrompt.CryptoObject(cipher)
            )
        }
    }

    override fun cancelAuthentication() {
        logDebug("cancelAuthentication", TAG)
        biometricPrompt?.cancelAuthentication()
    }

}