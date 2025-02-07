/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digital.sofia.NavActivityDirections
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.AppLanguage
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.repository.network.settings.SettingsRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.user.CheckUserForDeletionUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SupportBiometricManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach

class SettingsViewModel(
    private val preferences: PreferencesRepository,
    private val biometricManager: SupportBiometricManager,
    private val checkUserForDeletionUseCase: CheckUserForDeletionUseCase,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    cryptographyRepository: CryptographyRepository,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : BaseViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "SettingsViewModelTag"
    }

    override val isAuthorizationActive: Boolean = true

    private val _currentLanguageLiveData = MutableLiveData<AppLanguage>()
    val currentLanguageLiveData = _currentLanguageLiveData.readOnly()

    private val _authMethodDescriptionResLiveData = MutableLiveData<Int>()
    val authMethodDescriptionResLiveData = _authMethodDescriptionResLiveData.readOnly()

    fun onResume() {
        _currentLanguageLiveData.setValueOnMainThread(preferences.readCurrentLanguage())
        _authMethodDescriptionResLiveData.setValueOnMainThread(
            if (biometricManager.readyToBiometricAuth()) {
                R.string.auth_method_biometric
            } else {
                R.string.auth_method_pin
            }
        )
    }

    fun onProfileClicked() {
        logDebug("onProfileClicked", TAG)
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toProfileFragment(),
            viewModelScope
        )
    }

    fun onLanguageClicked() {
        logDebug("onLanguageClicked", TAG)
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toLanguageFragment(),
            viewModelScope
        )
    }

    fun onAuthMethodClicked() {
        logDebug("onAuthMethodClicked", TAG)
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toAuthMethodFragment(),
            viewModelScope
        )
    }

    fun onChangePinClicked() {
        logDebug("onChangePinClicked", TAG)
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toChangePinFlowFragment(),
            viewModelScope
        )
    }

    fun onDeleteProfileClicked() {
        logDebug("onDeleteProfileClicked", TAG)
        checkUserForDeletion()
    }

    private fun checkUserForDeletion() {
        logDebug("checkUserForDeletion", TAG)
       checkUserForDeletionUseCase.invoke().onEach { result ->
            result.onLoading {
                logDebug("checkUserForDeletion onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("checkUserForDeletion onSuccess", TAG)
                hideLoader()
                toConfirmDeleteProfile()
            }.onRetry {
                checkUserForDeletion()
            }.onFailure { failure ->
                logError("checkUserForDeletion onFailure", failure, TAG)
                hideLoader()
                when (failure.responseCode) {
                    409 -> toDeleteErrorProfile()
                    else -> showMessage(Message.error(R.string.error_server_error))
                }
            }
        }.launchInScope(viewModelScope)
    }

    private fun toConfirmDeleteProfile() {
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toDeleteProfileConfirmFragment(),
            viewModelScope
        )
    }

    private fun toDeleteErrorProfile() {
        findFlowNavController().navigateInMainThread(
            SettingsFragmentDirections.toDeleteProfileErrorFragment(),
            viewModelScope
        )
    }
}