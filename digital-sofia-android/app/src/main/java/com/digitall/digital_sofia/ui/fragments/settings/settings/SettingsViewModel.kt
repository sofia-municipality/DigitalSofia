package com.digitall.digital_sofia.ui.fragments.settings.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.common.AppLanguage
import com.digitall.digital_sofia.domain.models.common.BiometricStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class SettingsViewModel(
    private val currentContext: CurrentContext,
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
        private const val TAG = "SettingsViewModelTag"
    }

    override val needUpdateDocuments: Boolean = true

    private val _currentLanguageLiveData = MutableLiveData<AppLanguage>()
    val currentLanguageLiveData = _currentLanguageLiveData.readOnly()

    private val _authMethodDescriptionRes = MutableLiveData<Int>()
    val authMethodDescriptionRes = _authMethodDescriptionRes.readOnly()

    fun onResume() {
        _currentLanguageLiveData.value = preferences.readCurrentLanguage()
        val isBiometricAvailable = SupportBiometricManager.hasBiometrics(currentContext.get()) &&
                preferences.readPinCode()?.biometricStatus == BiometricStatus.BIOMETRIC
        _authMethodDescriptionRes.value = if (isBiometricAvailable) {
            R.string.auth_method_biometric
        } else {
            R.string.auth_method_pin
        }
    }

    fun onProfileClicked() {
        logDebug("onProfileClicked", TAG)
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toProfileFragment(), viewModelScope
        )
    }

    fun onLanguageClicked() {
        logDebug("onLanguageClicked", TAG)
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toLanguageFragment(), viewModelScope
        )
    }

    fun onAuthMethodClicked() {
        logDebug("onAuthMethodClicked", TAG)
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toAuthMethodFragment(), viewModelScope
        )
    }

    fun onChangePinClicked() {
        logDebug("onChangePinClicked", TAG)
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toChangePinFlowFragment(), viewModelScope
        )
    }

    fun onDeleteProfileClicked() {
        logDebug("onDeleteProfileClicked", TAG)
        logout()
    }
}