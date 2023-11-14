package com.digitall.digital_sofia.ui.fragments.registration.error

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.common.SdkStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationErrorViewModel(
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

    companion object {
        private const val TAG = "RegistrationErrorViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    private val _errorMessageResLiveData = SingleLiveEvent<Int>()
    val errorMessageResLiveData = _errorMessageResLiveData.readOnly()

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_READY -> {
                val pinCode = preferences.readPinCode()
                if (pinCode == null) {
                    logError("onSdkStatusChanged pinCode == null", TAG)
                    _errorMessageResLiveData.value = R.string.error_pin_code_not_setup
                    return
                }
                if (!pinCode.validate()) {
                    logError("onSdkStatusChanged !pinCode.validate()", TAG)
                    _errorMessageResLiveData.value = R.string.error_pin_code_not_setup
                    return
                }
                val user = preferences.readUser()
                if (user == null) {
                    logError("onSdkStatusChanged user == null", TAG)
                    _errorMessageResLiveData.value = R.string.error_user_not_setup_correct
                    return
                }
                if (!user.validate()) {
                    logError("onSdkStatusChanged !user.validate()", TAG)
                    _errorMessageResLiveData.value = R.string.error_user_not_setup_correct
                    return
                }
                toRegisterNewUserFragment()
            }

            SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_CANCELLED -> {
                toRegistrationDisagreeFragment()
            }

            else -> {
                // NO
            }
        }
    }

    private fun toRegistrationDisagreeFragment() {
        findFlowNavController().navigateInMainThread(
            RegistrationErrorFragmentDirections.toRegistrationDisagreeFragment(), viewModelScope
        )
    }

    private fun toRegisterNewUserFragment() {
        logDebug("toRegisterNewUserFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationErrorFragmentDirections.toRegistrationRegisterNewUserFragment(),
            viewModelScope
        )
    }
}