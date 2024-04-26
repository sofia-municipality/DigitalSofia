/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.error

import androidx.lifecycle.viewModelScope
import com.digital.sofia.NavActivityDirections
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.navigateNewRootInMainThread
import com.digital.sofia.ui.fragments.base.registration.error.BaseRegistrationErrorViewModel
import com.digital.sofia.ui.fragments.registration.confirm.RegistrationConfirmIdentificationFragmentDirections
import com.digital.sofia.ui.fragments.registration.confirm.RegistrationConfirmIdentificationViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.UpdateDocumentsHelper

class RegistrationErrorViewModel(
    private val preferences: PreferencesRepository,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : BaseRegistrationErrorViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "RegistrationErrorViewModelTag"
    }

    override fun onBackPressed() {
        logDebug("onBackClicked", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationErrorFragmentDirections.toEnterEgnFragment(),
            viewModelScope
        )
    }

    override fun proceedNext() {
        val user = preferences.readUser()
        if (user?.isVerified == true) {
            preferences.saveAppStatus(AppStatus.REGISTERED)
            navigateToMainTabs()
        } else {
            navigateToShareData()
        }
    }

    private fun navigateToMainTabs() {
        logDebug("toMainTabsFlowFragment", TAG)
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toMainTabsFlowFragment(),
            viewModelScope
        )
    }

    private fun navigateToShareData() {
        logDebug("toShareYourDataFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationErrorFragmentDirections.toShareYourDataFragment(),
            viewModelScope
        )
    }
}