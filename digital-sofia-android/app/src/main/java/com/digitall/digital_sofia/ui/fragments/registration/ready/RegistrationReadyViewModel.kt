package com.digitall.digital_sofia.ui.fragments.registration.ready

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.domain.extensions.capitalized
import com.digitall.digital_sofia.domain.models.common.AppLanguage
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.navigateNewRootInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationReadyViewModel(
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
        private const val TAG = "RegistrationReadyViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    private val _userNameLiveData = MutableLiveData<String>()
    val userNameLiveData = _userNameLiveData.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        _userNameLiveData.value = when (preferences.readCurrentLanguage()) {
            AppLanguage.BG -> preferences.readUser()?.firstName?.capitalized() ?: "потребител"
            AppLanguage.EN -> preferences.readUser()?.firstLatinName?.capitalized() ?: "user"
        }
    }

    fun onShowProfileClicked() {
        logDebug("onShowProfileClicked", TAG)
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toMainTabsFlowFragment(), viewModelScope
        )
    }
}