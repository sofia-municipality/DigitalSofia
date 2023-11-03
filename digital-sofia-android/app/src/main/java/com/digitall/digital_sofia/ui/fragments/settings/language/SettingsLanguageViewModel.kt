package com.digitall.digital_sofia.ui.fragments.settings.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

class SettingsLanguageViewModel(
    private val preferences: PreferencesRepository,
    private val localizationManager: LocalizationManager,
    logoutUseCase: LogoutUseCase,
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
        private const val TAG = "LSettingsLanguageViewModelTag"
    }

    override val needUpdateDocuments: Boolean = true

    private val _currentLanguageLiveData = MutableLiveData<AppLanguage>()
    val currentLanguageLiveData = _currentLanguageLiveData.readOnly()

    fun onResume() {
        _currentLanguageLiveData.value = preferences.readCurrentLanguage()
    }

    fun getReadyLiveData(): LiveData<Unit> {
        return localizationManager.readyLiveData
    }


    fun changeLanguage(language: AppLanguage) {
        logDebug("changeLanguage language: ${language.nameString}", TAG)
        _currentLanguageLiveData.value = language
        localizationManager.applyLanguage(language)
    }

}