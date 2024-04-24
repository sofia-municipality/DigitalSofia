/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.pin.enter

import androidx.lifecycle.viewModelScope
import com.digital.sofia.NavActivityDirections
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.BiometricStatus
import com.digital.sofia.domain.models.common.ErrorStatus
import com.digital.sofia.domain.models.common.PinCode
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.authorization.AuthorizationEnterToAccountUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.fragments.base.pin.enter.BaseEnterPinViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.onEach

class RegistrationEnterPinViewModel(
    private val preferences: PreferencesRepository,
    private val authorizationEnterToAccountUseCase: AuthorizationEnterToAccountUseCase,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    currentContext: CurrentContext,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
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
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "RegistrationEnterPinViewModelTag"
        private const val INVALID_USER = "User data is incorrect!"
        private const val LOGOUT_COUNT_EXCEED = "Login count exceeded!"
    }

    override val isAuthorizationActive: Boolean = false

    internal fun onForgotCodeClicked() {
        logDebug("onForgotCodeClicked", TAG)
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toForgotPinRegistrationFlowFragment(),
            viewModelScope
        )
    }

    override fun isBiometricAvailable(): Boolean {
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
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        val personalIdentificationNumber = user.personalIdentificationNumber
        if (personalIdentificationNumber.isNullOrEmpty()) {
            logError("checkCode user.personalIdentificationNumber.isNullOrEmpty()", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        val firebaseToken = preferences.readFirebaseToken()
        authorizationEnterToAccountUseCase.invoke(
            hashedPin = hashedPin,
            firebaseToken = firebaseToken?.token ?: "",
            personalIdentificationNumber = personalIdentificationNumber
        ).onEach { result ->
            result.onLoading {
                logDebug("checkCode onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("checkCode onSuccess", TAG)
                hideLoader()
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
            }.onRetry {
                checkCode(hashedPin = hashedPin, decryptedPin = decryptedPin)
            }.onFailure {
                logError("checkCode onFailure", it, TAG)
                hideLoader()
                resetCodeWhenNeeded()
                if (it.serverMessage == INVALID_USER) {
                    showMessage(
                        Message(
                            title = StringSource.Res(R.string.information),
                            message = StringSource.Res(R.string.error_invalid_user_data),
                            type = Message.Type.ALERT,
                            positiveButtonText = StringSource.Res(R.string.ok),
                        )
                    )
                } else if (it.serverMessage == LOGOUT_COUNT_EXCEED) {
                    showMessage(
                        Message(
                            title = StringSource.Res(R.string.information),
                            message = StringSource.Res(R.string.error_logout_count_exceeded),
                            type = Message.Type.ALERT,
                            positiveButtonText = StringSource.Res(R.string.ok),
                        )
                    )
                } else {
                    showMessage(Message.error(R.string.error_server_error))
                }
            }
        }.launchInScope(viewModelScope)
    }

    override fun navigateNext() {
        findFlowNavController().navigateInMainThread(
            RegistrationEnterPinFragmentDirections.toConfirmIdentificationFragment(),
            viewModelScope
        )
    }

}