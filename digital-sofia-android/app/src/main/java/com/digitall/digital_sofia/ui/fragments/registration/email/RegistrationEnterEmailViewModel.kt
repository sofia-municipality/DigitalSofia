package com.digitall.digital_sofia.ui.fragments.registration.email

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.BuildConfig
import com.digitall.digital_sofia.data.DEBUG_EMAIL
import com.digitall.digital_sofia.data.DEBUG_PHONE_NUMBER
import com.digitall.digital_sofia.domain.models.user.UserModel
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import java.util.regex.Pattern

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationEnterEmailViewModel(
    private val preferences: PreferencesRepository,
    logoutUseCase: LogoutUseCase,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
) : BaseViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
) {

    override val needUpdateDocuments: Boolean = false

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

    private val _savedPhoneLiveData = MutableLiveData<String>()
    val savedPhoneLiveData = _savedPhoneLiveData.readOnly()

    private val _savedEmailLiveData = MutableLiveData<String>()
    val savedEmailLiveData = _savedEmailLiveData.readOnly()

    private val _showEMailErrorLiveData = SingleLiveEvent<Boolean>()
    val showEMailErrorLiveData = _showEMailErrorLiveData.readOnly()

    private val _showPhoneErrorLiveData = SingleLiveEvent<Boolean>()
    val showPhoneErrorLiveData = _showPhoneErrorLiveData.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        if (BuildConfig.DEBUG && !DEBUG_EMAIL.isNullOrEmpty() && !DEBUG_PHONE_NUMBER.isNullOrEmpty()) {
            email = DEBUG_EMAIL!!
            phone = DEBUG_PHONE_NUMBER!!
            _savedEmailLiveData.value = DEBUG_EMAIL!!
            _savedPhoneLiveData.value = DEBUG_PHONE_NUMBER!!
            return
        }
        preferences.readUser()?.let {
            email = it.email ?: ""
            phone = it.phone ?: ""
            _savedEmailLiveData.value = it.email ?: ""
            _savedPhoneLiveData.value = it.phone ?: ""
        }
    }

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
        val user = preferences.readUser() ?: UserModel(
            securityContext = null,
            personalIdentificationNumber = null,
            countryCode2 = null,
            countryCode3 = null,
            phone = null,
            email = null,
            firstName = null,
            middleName = null,
            lastName = null,
            firstLatinName = null,
            middleLatinName = null,
            lastLatinName = null,
            isIdentified = null,
            isSupervised = null,
            isReadyToSign = null,
            isRejected = null,
        )
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
            _showEMailErrorLiveData.value = !isValidEmail
            _showPhoneErrorLiveData.value = !isValidPhone
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