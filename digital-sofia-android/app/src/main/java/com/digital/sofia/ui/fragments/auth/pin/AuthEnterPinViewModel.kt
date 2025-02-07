/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.auth.pin

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
import com.digital.sofia.ui.fragments.base.pin.enter.BaseEnterPinViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SupportBiometricManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthEnterPinViewModel(
    private val preferences: PreferencesRepository,
    private val biometricManager: SupportBiometricManager,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    currentContext: CurrentContext,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    cryptographyRepository: CryptographyRepository,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : BaseEnterPinViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    currentContext = currentContext,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "AuthEnterPinViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    var forceDisableBiometric = false

    private val _userNameLiveData = MutableLiveData<String>()
    val userNameLiveData = _userNameLiveData.readOnly()

    internal fun onForgotCodeClicked() {
        logDebug("onForgotCodeClicked", TAG)
        val pinCode = preferences.readPinCode()
        val user = preferences.readUser()
        if (pinCode == null) {
            logError("onForgotCodeClicked  pinCode == null", TAG)
            toRegistrationFragment()
            return
        }
        if (!pinCode.validate()) {
            logError("onForgotCodeClicked pinCode not valid", TAG)
            toRegistrationFragment()
            return
        }
        if (user == null) {
            logError("onForgotCodeClicked user == null", TAG)
            toRegistrationFragment()
            return
        }
        if (!user.validate()) {
            logError("onForgotCodeClicked user not valid", TAG)
            toRegistrationFragment()
            return
        }
        toForgotPinFragment()
    }

    private fun toForgotPinFragment() {
        logDebug("toForgotPinFragment", TAG)
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toForgotPinRegistrationFlowFragment(),
            viewModelScope
        )
    }

    override fun onCodeLocalCheckSuccess(hashedPin: String) {
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

    override fun onFirstAttach() {
        super.onFirstAttach()
        logDebug("onFirstAttach", TAG)
        _userNameLiveData.setValueOnMainThread(
            when (preferences.readCurrentLanguage()) {
                AppLanguage.BG -> preferences.readUser()?.firstName?.capitalized() ?: "потребител"
                AppLanguage.EN -> preferences.readUser()?.firstLatinName?.capitalized() ?: "user"
            }
        )
    }

    override fun isBiometricAvailable(): Boolean {
        return if (forceDisableBiometric) {
            logDebug("isBiometricAvailable forceDisableBiometric true", TAG)
            false
        } else {
            logDebug("isBiometricAvailable readyToBiometricAuth", TAG)
            biometricManager.readyToBiometricAuth()
        }
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
        logDebug("navigateNext", TAG)

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