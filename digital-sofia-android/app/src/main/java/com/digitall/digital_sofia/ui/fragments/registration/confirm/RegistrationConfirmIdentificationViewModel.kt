package com.digitall.digital_sofia.ui.fragments.registration.confirm

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.common.SdkStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.models.common.StringSource
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationConfirmIdentificationViewModel(
    private val preferences: PreferencesRepository,
    private val evrotrustSDKHelper: EvrotrustSDKHelper,
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
        private const val TAG = "RegistrationConfirmIdentificationViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_READY -> {
                val pinCode = preferences.readPinCode()
                if (pinCode == null) {
                    logError("onSdkStatusChanged pinCode == null", TAG)
                    toErrorFragment(R.string.error_pin_code_not_setup)
                    return
                }
                if (!pinCode.validate()) {
                    logError("onSdkStatusChanged !pinCode.validate()", TAG)
                    toErrorFragment(R.string.error_pin_code_not_setup)
                    return
                }
                val user = preferences.readUser()
                if (user == null) {
                    logError("onSdkStatusChanged user == null || !user.validate()", TAG)
                    toErrorFragment(R.string.error_user_not_setup_correct)
                    return
                }
                if (!user.validate()) {
                    logError("onCodeLocalCheckSuccess !user.validate())", TAG)
                    toErrorFragment(R.string.error_user_not_setup_correct)
                    return
                }
                toRegistrationRegisterNewUserFragment()
            }

            SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_CANCELLED -> {
                toRegistrationDisagreeFragment()
            }

            else -> {
                toErrorFragment(evrotrustSDKHelper.errorMessageRes ?: R.string.sdk_error_unknown)
            }
        }
    }

    private fun toRegistrationDisagreeFragment() {
        findFlowNavController().navigateInMainThread(
            RegistrationConfirmIdentificationFragmentDirections.toRegistrationDisagreeFragment(),
            viewModelScope
        )
    }

    private fun toRegistrationRegisterNewUserFragment() {
        logDebug("toSignDocumentFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationConfirmIdentificationFragmentDirections.toRegistrationRegisterNewUserFragment(),
            viewModelScope
        )
    }

    private fun toErrorFragment(@StringRes errorMessageRes: Int) {
        logDebug("toErrorFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationConfirmIdentificationFragmentDirections.toRegistrationErrorFragment(
                errorMessage = StringSource.Res(errorMessageRes)
            ), viewModelScope
        )
    }

    fun onNoClicked() {
        logDebug("onNoClicked", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationConfirmIdentificationFragmentDirections.toRegistrationDisagreeFragment(),
            viewModelScope
        )
    }
}