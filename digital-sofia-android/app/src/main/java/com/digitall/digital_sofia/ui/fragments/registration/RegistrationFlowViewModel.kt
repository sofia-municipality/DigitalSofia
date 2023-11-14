package com.digitall.digital_sofia.ui.fragments.registration

import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.common.AppStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.models.common.StartDestination
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationFlowViewModel(
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
        private const val TAG = "RegistrationFlowViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    fun getStartDestination(): StartDestination {
        val user = preferences.readUser()
        val appStatus = preferences.readAppStatus()
        val pinCode = preferences.readPinCode()
        return if (
            (appStatus == AppStatus.NOT_SIGNED_DOCUMENT ||
                    appStatus == AppStatus.NOT_SEND_SIGNED_DOCUMENT) &&
            user != null &&
            user.validate() &&
            pinCode != null &&
            pinCode.validate()
        ) {
            StartDestination(R.id.registrationShareYourDataFragment)
        } else {
            StartDestination(R.id.registrationStartFragment)
        }
    }

}