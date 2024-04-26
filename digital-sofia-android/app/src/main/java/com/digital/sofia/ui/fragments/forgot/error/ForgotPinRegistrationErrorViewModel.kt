package com.digital.sofia.ui.fragments.forgot.error

import androidx.lifecycle.viewModelScope
import com.digital.sofia.NavActivityDirections
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.models.user.UserModel
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.registration.RegistrationRegisterNewUserUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.navigateNewRootInMainThread
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.fragments.base.registration.error.BaseRegistrationErrorViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.onEach

class ForgotPinRegistrationErrorViewModel(
    private val preferences: PreferencesRepository,
    private val registrationRegisterNewUserUseCase: RegistrationRegisterNewUserUseCase,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : BaseRegistrationErrorViewModel(
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
        private const val TAG = "ForgotPinRegistrationErrorViewModelTag"
    }

    override fun onBackPressed() {
        logDebug("onBackClicked", TAG)
        toRegistrationFragment()
    }

    override fun proceedNext() {
        logDebug("proceedNext", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("proceedNext pinCode == null", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            toRegistrationFragment()
            return
        }
        if (!pinCode.validate()) {
            logError("proceedNext !pinCode.validate()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            toRegistrationFragment()
            return
        }
        val user = preferences.readUser()
        if (user == null) {
            logError("proceedNext user == null", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            toRegistrationFragment()
            return
        }
        if (!user.validate()) {
            logError("proceedNext !user.validate())", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            toRegistrationFragment()
            return
        }
        val firebaseToken = preferences.readFirebaseToken()
        if (firebaseToken == null || firebaseToken.token.isEmpty()) {
            logError("registerNewUser fcm.isNullOrEmpty", TAG)
        }
        registerNewUser(user)
    }

    private fun registerNewUser(user: UserModel) {
        logDebug("registerNewUser", TAG)
        registrationRegisterNewUserUseCase.invoke(
            personalIdentificationNumber = user.personalIdentificationNumber!!,
        ).onEach { result ->
            result.onLoading {
                logDebug("registerNewUser onLoading", TAG)
                showLoader()
                hideErrorState()
            }.onSuccess {
                logDebug("registerNewUser onSuccess", TAG)
                toMainTabs()
            }.onRetry {
                registerNewUser(user = user)
            }.onFailure {
                logError("registerNewUser onFailure", it, TAG)
                hideLoader()
                showMessage(Message.error(R.string.error_server_error))
            }
        }.launchInScope(viewModelScope)
    }

    private fun toMainTabs() {
        preferences.saveAppStatus(AppStatus.REGISTERED)
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toMainTabsFlowFragment(),
            viewModelScope
        )
    }

}