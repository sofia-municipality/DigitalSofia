package com.digital.sofia.ui.fragments.forgot

import com.digital.sofia.R
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StartDestination
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.ui.fragments.registration.RegistrationFlowViewModel
import com.digital.sofia.ui.fragments.registration.RegistrationFlowViewModel.Companion
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager

class ForgotPinRegistrationFlowViewModel(
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    private val preferences: PreferencesRepository,
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
        private const val TAG = "ForgotPinRegistrationFlowViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    fun getStartDestination(): StartDestination {
        return when (preferences.readAppStatus()) {
            AppStatus.PROFILE_VERIFICATION_FORGOTTEN_PIN -> StartDestination(R.id.forgotPinShareYourDataFragment)

            else -> {
                StartDestination(R.id.forgotPinRegistrationCreatePinFragment)
            }
        }
    }

}