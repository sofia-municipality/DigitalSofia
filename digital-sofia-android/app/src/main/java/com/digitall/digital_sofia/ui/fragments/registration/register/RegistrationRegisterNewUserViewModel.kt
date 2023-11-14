package com.digitall.digital_sofia.ui.fragments.registration.register

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.common.AppStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.usecase.registration.RegistrationUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.onEach

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationRegisterNewUserViewModel(
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
        private const val TAG = "RegistrationRegisterNewUserViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    fun registerNewUser() {
        logDebug("updateDocuments", TAG)
        val user = preferences.readUser()
        if (user == null) {
            logError("registerNewUser user == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        if (!user.validate()) {
            logError("registerNewUser !user.validate())", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("registerNewUser pinCode == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (!pinCode.validate()) {
            logError("registerNewUser !pinCode.validate()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            logout()
            return
        }
        if (preferences.readFirebaseToken().isNullOrEmpty()) {
            logError("registerNewUser fcm.isNullOrEmpty", TAG)
        }
        registrationUseCase.registerNewUser(
            email = user.email ?: "null",
            phoneNumber = user.phone!!,
            hashedPin = pinCode.hashedPin!!,
            firebaseToken = preferences.readFirebaseToken() ?: "null",
            personalIdentificationNumber = user.personalIdentificationNumber!!,
        ).onEach { result ->
            result.onLoading {
                logDebug("updateDocuments onLoading", TAG)
                hideErrorState()
            }.onSuccess {
                logDebug("updateDocuments onSuccess", TAG)
                hideErrorState()
                preferences.saveAppStatus(AppStatus.NOT_SIGNED_DOCUMENT)
                toShareYourDataFragment()
            }.onFailure {
                logError("updateDocuments onFailure", TAG)
                showErrorState(showReloadButton = true)
                preferences.saveAppStatus(AppStatus.NOT_READY)
            }
        }.launch(viewModelScope)
    }

    private fun toShareYourDataFragment() {
        logDebug("toShareYourDataFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationRegisterNewUserFragmentDirections.toRegistrationShareYourDataFragment(),
            viewModelScope
        )
    }

}