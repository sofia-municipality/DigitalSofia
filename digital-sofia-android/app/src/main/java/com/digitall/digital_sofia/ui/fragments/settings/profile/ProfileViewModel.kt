package com.digitall.digital_sofia.ui.fragments.settings.profile

import androidx.lifecycle.MutableLiveData
import com.digitall.digital_sofia.domain.extensions.capitalized
import com.digitall.digital_sofia.domain.models.common.AppLanguage
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ProfileViewModel(
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
        private const val TAG = "ProfileViewModelTag"
    }

    override val needUpdateDocuments: Boolean = true

    private val _userNameLiveData = MutableLiveData<String>()
    val userNameLiveData = _userNameLiveData.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        val user = preferences.readUser()
        val userName = when (preferences.readCurrentLanguage()) {
            AppLanguage.BG -> "${user?.firstName?.capitalized()} ${user?.middleName?.capitalized()} ${user?.lastName?.capitalized()}"
            AppLanguage.EN -> "${user?.firstLatinName?.capitalized()} ${user?.middleLatinName?.capitalized()} ${user?.lastLatinName?.capitalized()}"
        }
        logDebug("userName: $userName", TAG)
        _userNameLiveData.value = userName
    }

}