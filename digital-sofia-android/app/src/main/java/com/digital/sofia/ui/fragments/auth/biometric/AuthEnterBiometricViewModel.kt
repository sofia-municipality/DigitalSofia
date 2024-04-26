/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.auth.biometric

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digital.sofia.NavActivityDirections
import com.digital.sofia.R
import com.digital.sofia.domain.extensions.capitalized
import com.digital.sofia.domain.models.common.AppLanguage
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.isFragmentInBackStack
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.navigateNewRootInMainThread
import com.digital.sofia.extensions.popBackStackInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SingleLiveEvent
import com.digital.sofia.utils.SupportBiometricManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.crypto.Cipher

class AuthEnterBiometricViewModel(
    private val preferences: PreferencesRepository,
    private val biometricManager: SupportBiometricManager,
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
        private const val TAG = "EnterBiometricViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    private val _userNameLiveData = MutableLiveData<String>()
    val userNameLiveData = _userNameLiveData.readOnly()

    private val _startBiometricAuthLiveData = SingleLiveEvent<Cipher>()
    val startBiometricAuthLiveData = _startBiometricAuthLiveData.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        if (!biometricManager.readyToBiometricAuth()) {
            logError("not readyToBiometricAuth", TAG)
            toEnterPinFragment(forceDisableBiometric = true)
            return
        }
        val user = preferences.readUser()
        _userNameLiveData.setValueOnMainThread(
            when (preferences.readCurrentLanguage()) {
                AppLanguage.BG -> user?.firstName?.capitalized() ?: "потребител"
                AppLanguage.EN -> user?.firstLatinName?.capitalized() ?: "user"
            }
        )
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
            disableBiometric()
            logError("startBiometricAuth !pinCode.validateWithEncrypted()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            toEnterPinFragment(true)
            return
        }
        val cipher = getBiometricCipherForDecryption()
        if (cipher == null) {
            logError("startBiometricAuth cipher == null", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            toEnterPinFragment(true)
            return
        }
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
            toEnterPinFragment(true)
            return
        }
        if (cipher == null) {
            logError("onBiometricSuccess cipher == null", TAG)
            showMessage(Message.error(R.string.error_biometric))
            toEnterPinFragment(true)
            return
        }
        try {
            val decryptedPin = cryptographyRepository.decrypt(pinCode.encryptedPin!!, cipher)
            if (decryptedPin.isEmpty()) {
                logError("onBiometricSuccess decryptedPin.isEmpty()", TAG)
                showMessage(Message.error(R.string.error_biometric))
                toEnterPinFragment(true)
                return
            }
            if (decryptedPin != pinCode.decryptedPin) {
                logError("onBiometricSuccess decryptedPin != pinCode.decryptedPin", TAG)
                showMessage(Message.error(R.string.error_biometric))
                toEnterPinFragment(true)
                return
            }
            onCodeLocalCheckSuccess(
                hashedPin = pinCode.hashedPin!!
            )
        } catch (e: Exception) {
            logError(e, TAG)
            showMessage(Message.error(R.string.error_biometric))
            toEnterPinFragment(true)
        }
    }

    private fun onCodeLocalCheckSuccess(hashedPin: String) {
        logDebug("onCodeLocalCheckSuccess hashedPin: $hashedPin", TAG)
        val user = preferences.readUser()
        if (user == null) {
            logError("onCodeLocalCheckSuccess user == null", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        if (!user.validate()) {
            logError("onCodeLocalCheckSuccess !user.validate())", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        navigateNext()
    }

    fun onBiometricTooManyAttempts() {
        logError("onBiometricTooManyAttempts", TAG)
        showMessage(Message.error(R.string.auth_biometric_scanner_many_attempts))
        toEnterPinFragment(true)
    }

    fun onBiometricError() {
        logError("onBiometricError", TAG)
        showMessage(Message.error(R.string.auth_biometric_scanner_failed))
        toEnterPinFragment(true)
    }

    fun onBiometricException() {
        logError("onBiometricException", TAG)
        showMessage(Message.error(R.string.error_biometric))
        toEnterPinFragment(true)
    }

    fun toEnterPinFragmentOnDisabledBiometrics() {
        findActivityNavController().popBackStackInMainThread(viewModelScope)
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toEnterCodeFlowFragment(), viewModelScope
        )
    }

    private fun navigateNext() {
        logDebug("toMainTabs", TAG)
        val isMainTabsFragmentInStack =
            findActivityNavController().isFragmentInBackStack(R.id.mainTabsFlowFragment)
        if (isMainTabsFragmentInStack) {
            findActivityNavController().popBackStackInMainThread(viewModelScope)
        } else {
            findActivityNavController().navigateNewRootInMainThread(
                NavActivityDirections.toMainTabsFlowFragment(),
                viewModelScope
            )
        }
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
            ),
            viewModelScope
        )
        viewModelScope.launch {
            delay(1000)
            hideLoader()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val isMainTabsFragmentInStack =
            findActivityNavController().isFragmentInBackStack(R.id.mainTabsFlowFragment)
        if (isMainTabsFragmentInStack) {
            closeActivity()
        } else {
            finishFlow()
        }
    }
}