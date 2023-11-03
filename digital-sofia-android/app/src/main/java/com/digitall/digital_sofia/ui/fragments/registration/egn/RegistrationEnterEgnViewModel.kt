package com.digitall.digital_sofia.ui.fragments.registration.egn

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.data.BuildConfig
import com.digitall.digital_sofia.data.DEBUG_PERSONAL_IDENTIFICATION_NUMBER
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.user.UserModel
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.usecase.registration.RegistrationUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationEnterEgnViewModel(
    private val preferences: PreferencesRepository,
    private val registrationUseCase: RegistrationUseCase,
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

    companion object {
        private const val TAG = "RegistrationEnterEgnViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    private var showErrors = false

    private var conditionsIsChecked = false
    private var personalIdentificationNumber = ""

    private val _savedEgnLiveData = MutableLiveData<String>()
    val savedEgnLiveData = _savedEgnLiveData.readOnly()

    private val _showEgnErrorLiveData = SingleLiveEvent<Boolean>()
    val showEgnErrorLiveData = _showEgnErrorLiveData.readOnly()

    private val _showConditionsErrorLiveData = SingleLiveEvent<Boolean>()
    val showConditionsErrorLiveData = _showConditionsErrorLiveData.readOnly()

    fun setPersonalIdentificationNumber(personalIdentificationNumber: String) {
        logDebug(
            "setPersonalIdentificationNumber personalIdentificationNumber: $personalIdentificationNumber",
            TAG
        )
        this.personalIdentificationNumber = personalIdentificationNumber
        updateSavedUser()
        if (showErrors) {
            validateInput()
        }
    }

    fun setConditionsIsChecked(conditionsIsChecked: Boolean) {
        logDebug("setConditionsIsChecked conditionsIsChecked: $conditionsIsChecked", TAG)
        this.conditionsIsChecked = conditionsIsChecked
        if (showErrors) {
            validateInput()
        }
    }

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        if (BuildConfig.DEBUG && !DEBUG_PERSONAL_IDENTIFICATION_NUMBER.isNullOrEmpty()) {
            personalIdentificationNumber = DEBUG_PERSONAL_IDENTIFICATION_NUMBER!!
            _savedEgnLiveData.value = DEBUG_PERSONAL_IDENTIFICATION_NUMBER!!
            return
        }
        preferences.readUser()?.let {
            personalIdentificationNumber = it.personalIdentificationNumber ?: ""
            _savedEgnLiveData.value = it.personalIdentificationNumber ?: ""
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
        if (isValidOPersonalIdentificationNumber()) {
            user.personalIdentificationNumber = personalIdentificationNumber
        }
        preferences.saveUser(user)
    }

    fun onNextClicked() {
        logDebug("onNextClicked", TAG)
        showErrors = true
        if (validateInput()) {
            registrationUseCase.checkUser(
                personalIdentificationNumber = personalIdentificationNumber
            ).onEach { result ->
                result.onLoading {
                    logDebug("checkUserByPersonalIdentificationNumber onLoading", TAG)
                    showLoader()
                }.onSuccess {
                    logDebug(
                        "checkUserByPersonalIdentificationNumber onSuccess userExist: ${it.userExist} hasPin: ${it.hasPin}",
                        TAG
                    )
                    hideLoader()
                    when {
                        it.userExist == true && it.hasPin == true -> {
                            findFlowNavController().navigateInMainThread(
                                RegistrationEnterEgnFragmentDirections.toEnterPinFragment(),
                                viewModelScope
                            )
                        }

                        it.userExist == true && it.hasPin != true -> {
                            findFlowNavController().navigateInMainThread(
                                RegistrationEnterEgnFragmentDirections.toCreatePinFragment(),
                                viewModelScope
                            )
                        }

                        else -> {
                            findFlowNavController().navigateInMainThread(
                                RegistrationEnterEgnFragmentDirections.toEnterEmailFragment(),
                                viewModelScope
                            )
                        }
                    }
                }.onFailure {
                    logError("checkUserByPersonalIdentificationNumber onFailure", TAG)
                    hideLoader()
                    showBannerMessage(BannerMessage.error("Error"))
                }
            }.launch(viewModelScope)
        }
    }

    private fun validateInput(): Boolean {
        val isValidOPersonalIdentificationNumber = isValidOPersonalIdentificationNumber()
        if (showErrors) {
            _showEgnErrorLiveData.value = !isValidOPersonalIdentificationNumber()
            _showConditionsErrorLiveData.value = !conditionsIsChecked
        }
        return conditionsIsChecked &&
                isValidOPersonalIdentificationNumber
    }

    private fun isValidOPersonalIdentificationNumber(): Boolean {
        return personalIdentificationNumber.length == 10 && checkValidationCode() && checkValidDate()
    }

    private fun checkValidationCode(): Boolean {
        return try {
            val weight = intArrayOf(2, 4, 8, 5, 10, 9, 7, 3, 6)
            val mySum = weight.indices.sumBy {
                weight[it] * personalIdentificationNumber[it].toString().toInt()
            }
            personalIdentificationNumber.last().toString() == (mySum % 11).toString().last()
                .toString()
        } catch (e: NumberFormatException) {
            false
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun checkValidDate(): Boolean {
        try {
            val year = personalIdentificationNumber.substring(0, 2).toInt()
            val month = personalIdentificationNumber.substring(2, 4).toInt()
            val day = personalIdentificationNumber.substring(4, 6).toInt()
            val adjustedYear: Int = when {
                month >= 40 -> year + 2000
                month >= 20 -> year + 1800
                else -> year + 1900
            }
            val dateString = "$adjustedYear-$month-$day"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            dateFormat.isLenient = false
            dateFormat.parse(dateString)
            return true
        } catch (e: Exception) {
            return false
        }
    }

}