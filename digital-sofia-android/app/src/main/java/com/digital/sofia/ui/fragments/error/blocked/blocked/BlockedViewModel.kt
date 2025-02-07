/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.error.blocked.blocked

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digital.sofia.NavActivityDirections
import com.digital.sofia.R
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.navigateNewRootInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlockedViewModel(
    private val currentContext: CurrentContext,
    private val preferences: PreferencesRepository,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    localizationManager: LocalizationManager,
    authorizationHelper: AuthorizationHelper,
    cryptographyRepository: CryptographyRepository,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
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
        private const val TAG = "BlockedViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    private val _blockedText = MutableLiveData<String>()
    val blockedText = _blockedText.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode?.errorTimeCode == null) {
            logout()
            return
        }
        val timeOnUnlock = pinCode.errorTimeCode!! + pinCode.errorStatus.timeoutMillis
        logDebug("timeOnUnlock: $timeOnUnlock", TAG)
        logDebug("currentTimeMillis: ${System.currentTimeMillis()}", TAG)
        if (System.currentTimeMillis() < timeOnUnlock) {
            logDebug("System.currentTimeMillis() < timeOnUnlock", TAG)
            viewModelScope.launch {
                while (System.currentTimeMillis() < timeOnUnlock) {
                    val timeLeft = (timeOnUnlock - System.currentTimeMillis()) / 1000L
                    _blockedText.setValueOnMainThread(
                        currentContext.get().getString(
                            R.string.error_blocked_description,
                            timeLeft.toString()
                        )
                    )
                    delay(1000)
                }
                findActivityNavController().navigateNewRootInMainThread(
                    NavActivityDirections.toEnterCodeFlowFragment(),
                    viewModelScope
                )
            }
        } else {
            logDebug("System.currentTimeMillis() > timeOnUnlock", TAG)
            findActivityNavController().navigateNewRootInMainThread(
                NavActivityDirections.toEnterCodeFlowFragment(),
                viewModelScope
            )
        }
    }
}