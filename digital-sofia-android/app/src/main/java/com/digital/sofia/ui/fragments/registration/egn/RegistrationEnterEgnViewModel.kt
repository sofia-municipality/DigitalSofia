/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.egn

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.registration.CheckPersonalIdentificationNumberModel
import com.digital.sofia.domain.models.user.UserModel
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.registration.RegistrationCheckUserUseCase
import com.digital.sofia.domain.usecase.registration.RegistrationRegisterNewUserUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
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
import com.digital.sofia.utils.UpdateDocumentsHelper
import com.digital.sofia.utils.isValidOPersonalIdentificationNumber
import kotlinx.coroutines.flow.onEach

class RegistrationEnterEgnViewModel(
    private val preferences: PreferencesRepository,
    private val registrationCheckUserUseCase: RegistrationCheckUserUseCase,
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
        private const val TAG = "RegistrationEnterEgnViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    private var showErrors = false

    private var conditionsIsChecked = false
    private var personalIdentificationNumber = ""

    private val _showEgnErrorLiveData = SingleLiveEvent<Boolean>()
    val showEgnErrorLiveData = _showEgnErrorLiveData.readOnly()

    private val _showConditionsErrorLiveData = SingleLiveEvent<Boolean>()
    val showConditionsErrorLiveData = _showConditionsErrorLiveData.readOnly()

    private val _buttonNextEnabledLiveData = SingleLiveEvent(false)
    val buttonNextEnabledLiveData = _buttonNextEnabledLiveData.readOnly()

    fun setPersonalIdentificationNumber(personalIdentificationNumber: String) {
        logDebug("setPersonalIdentificationNumber", TAG)
        this.personalIdentificationNumber = personalIdentificationNumber
        updateNextButtonState()
        if (showErrors) {
            validateInput()
        }

    }

    fun setConditionsIsChecked(conditionsIsChecked: Boolean) {
        logDebug("setConditionsIsChecked conditionsIsChecked: $conditionsIsChecked", TAG)
        this.conditionsIsChecked = conditionsIsChecked
        updateNextButtonState()
        if (showErrors) {
            validateInput()
        }
    }

    private fun updateNextButtonState() {
        logDebug("updateNextButtonState", TAG)
        val isEnabled = conditionsIsChecked && personalIdentificationNumber.isNotEmpty()
        _buttonNextEnabledLiveData.setValueOnMainThread(isEnabled)
    }

    fun onNextClicked() {
        logDebug("onNextClicked", TAG)
        if (personalIdentificationNumber.isEmpty()) {
            logError("onNextClicked personalIdentificationNumber isEmpty", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            return
        }
        showErrors = true
        if (validateInput()) {
            registrationCheckUserUseCase.invoke(
                personalIdentificationNumber = personalIdentificationNumber
            ).onEach { result ->
                result.onLoading {
                    logDebug("checkUserByPersonalIdentificationNumber onLoading", TAG)
                    showLoader()
                }.onSuccess { model ->
                    logDebug("checkUserByPersonalIdentificationNumber onSuccess", TAG)
                    onCheckUserSuccess(model)
                }.onRetry {
                    onNextClicked()
                }.onFailure {
                    logError("checkUserByPersonalIdentificationNumber onFailure", it, TAG)
                    hideLoader()
                    showMessage(Message.error(R.string.error_server_error))
                }
            }.launchInScope(viewModelScope)
        }
    }

    private fun onCheckUserSuccess(model: CheckPersonalIdentificationNumberModel) {
        logDebug("onCheckUserSuccess", TAG)
        when {
            model.userExist == true && model.hasPin == true -> onUserAndPinExist(model)
            model.userExist == true && model.hasPin != true -> onUserExist(model)
            else -> onUserNotExist()
        }
        getLogLevel()
    }

    private fun onUserNotExist() {
        logDebug("onUserNotExist", TAG)
        registrationRegisterNewUserUseCase.invoke(
            personalIdentificationNumber = personalIdentificationNumber
        ).onEach { result ->
            result.onLoading {
                logDebug("onUserNotExist onLoading", TAG)
            }.onSuccess {
                logDebug("onUserNotExist onSuccess", TAG)
                onUserCreateReady()
            }.onRetry {
                onUserNotExist()
            }.onFailure {
                logError("onUserNotExist onFailure", it, TAG)
                hideLoader()
                if (it.responseCode == 403) {
                    showMessage(Message.error(R.string.registration_enter_egn_conditions_error_evrotrust))
                } else {
                    showMessage(Message.error(R.string.error_server_error))
                }
            }
        }.launchInScope(viewModelScope)
    }

    private fun onUserCreateReady() {
        logDebug("onUserCreateReady", TAG)
        val user = UserModel(
            phone = null,
            email = null,
            lastName = null,
            firstName = null,
            middleName = null,
            isRejected = null,
            isVerified = false,
            countryCode2 = null,
            countryCode3 = null,
            isIdentified = null,
            isSupervised = null,
            isReadyToSign = null,
            lastLatinName = null,
            firstLatinName = null,
            middleLatinName = null,
            securityContext = null,
            personalIdentificationNumber = personalIdentificationNumber,
            isDebug = false,
        )
        preferences.saveUser(user)
        preferences.savePinCode(null)
        hideLoader()
        toEnterEmailFragment()
    }

    private fun onUserExist(model: CheckPersonalIdentificationNumberModel) {
        logDebug("onUserExist", TAG)
        hideLoader()
        val user = UserModel(
            phone = null,
            email = null,
            lastName = null,
            firstName = null,
            middleName = null,
            isRejected = null,
            isVerified = model.isVerified,
            countryCode2 = null,
            countryCode3 = null,
            isIdentified = null,
            isSupervised = null,
            isReadyToSign = null,
            lastLatinName = null,
            firstLatinName = null,
            middleLatinName = null,
            securityContext = null,
            personalIdentificationNumber = personalIdentificationNumber,
            isDebug = false,
        )
        preferences.saveUser(user)
        preferences.savePinCode(null)
        hideLoader()
        if (model.hasContactInfo == true) {
            toCreatePinFragment()
        } else {
            toEnterEmailFragment()
        }
    }

    private fun onUserAndPinExist(model: CheckPersonalIdentificationNumberModel) {
        logDebug("onUserAndPinExist", TAG)
        val user = UserModel(
            phone = null,
            email = null,
            lastName = null,
            firstName = null,
            middleName = null,
            isRejected = null,
            isVerified = model.isVerified,
            countryCode2 = null,
            countryCode3 = null,
            isIdentified = null,
            isSupervised = null,
            isReadyToSign = null,
            lastLatinName = null,
            firstLatinName = null,
            middleLatinName = null,
            securityContext = null,
            personalIdentificationNumber = personalIdentificationNumber,
            isDebug = false,
        )
        preferences.saveUser(user)
        preferences.savePinCode(null)
        hideLoader()
        toEnterPinFragment()
    }

    private fun toEnterPinFragment() {
        logDebug("toEnterPinFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationEnterEgnFragmentDirections.toEnterPinFragment(),
            viewModelScope
        )
    }

    private fun toCreatePinFragment() {
        logDebug("toCreatePinFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationEnterEgnFragmentDirections.toCreatePinFragment(),
            viewModelScope
        )

    }

    private fun toEnterEmailFragment() {
        logDebug("toEnterEmailFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationEnterEgnFragmentDirections.toEnterEmailFragment(),
            viewModelScope
        )
    }

    private fun validateInput(): Boolean {
        logDebug("validateInput", TAG)
        val isValidOPersonalIdentificationNumber =
            isValidOPersonalIdentificationNumber(personalIdentificationNumber)
        if (showErrors) {
            _showEgnErrorLiveData.setValueOnMainThread(!isValidOPersonalIdentificationNumber)
            _showConditionsErrorLiveData.setValueOnMainThread(!conditionsIsChecked)
        }
        return conditionsIsChecked &&
                isValidOPersonalIdentificationNumber
    }

}