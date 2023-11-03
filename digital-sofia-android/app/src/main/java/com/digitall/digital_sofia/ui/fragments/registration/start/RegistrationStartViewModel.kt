package com.digitall.digital_sofia.ui.fragments.registration.start

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationStartViewModel(
    logoutUseCase: LogoutUseCase,
    preferences: PreferencesRepository,
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
        private const val TAG = "RegistrationStartViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    fun onRegistrationClicked() {
        logDebug("onRegistrationClicked", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationStartFragmentDirections.toEnterEgnFragment(), viewModelScope
        )
    }

}