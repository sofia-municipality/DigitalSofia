/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.email

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.navigateInMainThread
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
import java.util.regex.Pattern

class RegistrationEnterEmailViewModel(
    private val preferences: PreferencesRepository,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    cryptographyRepository: CryptographyRepository,
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
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    override val isAuthorizationActive: Boolean = false

    companion object {
        private const val TAG = "RegistrationEnterEmailViewModelTag"
        private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        // +359 YY YXXX XXX
        private val PHONE_PATTERN = Pattern.compile(
            "^\\+[0-9]{12}\$"
        )
    }

    private var showErrors = false
    private var email = ""
    private var phone = ""

    private val _showEMailErrorLiveData = SingleLiveEvent<Boolean>()
    val showEMailErrorLiveData = _showEMailErrorLiveData.readOnly()

    private val _showPhoneErrorLiveData = SingleLiveEvent<Boolean>()
    val showPhoneErrorLiveData = _showPhoneErrorLiveData.readOnly()

    fun setEmail(email: String) {
        logDebug("setEmail email: $email", TAG)
        this.email = email
        updateSavedUser()
        if (showErrors) {
            validateInput()
        }
    }

    fun setPhone(phone: String) {
        logDebug("setPhone phone: $phone", TAG)
        this.phone = phone
        updateSavedUser()
        if (showErrors) {
            validateInput()
        }
    }

    private fun updateSavedUser() {
        logDebug("updateSavedUser", TAG)
        val user = preferences.readUser()
        if (user == null) {
            logError("updateSavedUser user == null", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            return
        }
        if (isValidEmail()) {
            user.email = email
        }
        if (isValidPhone()) {
            user.phone = phone
        }
        preferences.saveUser(user)
    }

    fun onNextClicked() {
        logDebug("onNextClicked", TAG)
        showErrors = true
        if (validateInput()) {
            findFlowNavController().navigateInMainThread(
                RegistrationEnterEmailFragmentDirections.toCreatePinFragment(), viewModelScope
            )
        }
    }

    private fun validateInput(): Boolean {
        val isValidEmail = isValidEmail()
        val isValidPhone = isValidPhone()
        if (showErrors) {
            _showEMailErrorLiveData.setValueOnMainThread(!isValidEmail)
            _showPhoneErrorLiveData.setValueOnMainThread(!isValidPhone)
        }
        return isValidEmail &&
                isValidPhone
    }

    private fun isValidEmail(): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    private fun isValidPhone(): Boolean {
        return PHONE_PATTERN.matcher(phone).matches() && phone.startsWith("+359")
    }

}