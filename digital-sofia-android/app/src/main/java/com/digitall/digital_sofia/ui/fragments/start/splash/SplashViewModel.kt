package com.digitall.digital_sofia.ui.fragments.start.splash

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.BuildConfig
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.data.DEBUG_FORCE_ENTER_TO_ACCOUNT
import com.digitall.digital_sofia.data.DEBUG_LOGOUT_FROM_PREFERENCES
import com.digitall.digital_sofia.data.DEBUG_PRINT_PREFERENCES_INFO
import com.digitall.digital_sofia.domain.models.common.AppStatus
import com.digitall.digital_sofia.domain.models.common.ErrorStatus
import com.digitall.digital_sofia.domain.models.common.SdkStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.navigateNewRootInMainThread
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.StringSource
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class SplashViewModel(
    private val preferences: PreferencesRepository,
    private val evrotrustSDKHelper: EvrotrustSDKHelper,
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
        private const val TAG = "SplashViewModelTag"
        private const val PREFERENCES_INFO_TAG = "PreferencesInfoTag"
    }

    override val needUpdateDocuments: Boolean = false

    override fun onFirstAttach() {
        if (BuildConfig.DEBUG && DEBUG_LOGOUT_FROM_PREFERENCES) {
            preferences.logoutFromPreferences()
            logDebug("logoutFromPreferences", TAG)
        }
        if (BuildConfig.DEBUG && DEBUG_PRINT_PREFERENCES_INFO) {
            logDebug("PRINT_PREFERENCES_INFO", TAG)
            val pinCode = preferences.readPinCode()
            logDebug("hashedPin: ${pinCode?.hashedPin}", PREFERENCES_INFO_TAG)
            logDebug("encryptedPin: ${pinCode?.encryptedPin}", PREFERENCES_INFO_TAG)
            val appStatus = preferences.readAppStatus()
            logDebug("appStatus: ${appStatus.name}", PREFERENCES_INFO_TAG)
            val user = preferences.readUser()
            logDebug("firstName: ${user?.firstName}", PREFERENCES_INFO_TAG)
            logDebug("lastName: ${user?.lastName}", PREFERENCES_INFO_TAG)
            logDebug("middleName: ${user?.middleName}", PREFERENCES_INFO_TAG)
            logDebug("phone: ${user?.phone}", PREFERENCES_INFO_TAG)
            logDebug("email: ${user?.email}", PREFERENCES_INFO_TAG)
            logDebug("securityContext: ${user?.securityContext}", PREFERENCES_INFO_TAG)
            logDebug(
                "personalIdentificationNumber: ${user?.personalIdentificationNumber}",
                PREFERENCES_INFO_TAG
            )
        }
    }

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        val appStatus = preferences.readAppStatus()
        val pinCode = preferences.readPinCode()
        val user = preferences.readUser()
        when (sdkStatus) {
            SdkStatus.SDK_SETUP_READY -> {
                logDebug("sdkStatus SDK_SETUP_READY", TAG)
                if (BuildConfig.DEBUG && DEBUG_FORCE_ENTER_TO_ACCOUNT) {
                    preferences.saveAppStatus(AppStatus.READY)
                    toEnterCodeFragment()
                    return
                }
                if (appStatus != AppStatus.READY) {
                    logError("appStatus not ready", TAG)
                    toRegistrationFragment()
                    return
                }
                if (pinCode == null) {
                    logError("onSdkStatusChanged pinCode == null", TAG)
                    toRegistrationFragment()
                    return
                }
                if (!pinCode.validate()) {
                    logError("onSdkStatusChanged !pinCode.validate()", TAG)
                    toRegistrationFragment()
                    return
                }
                if (user == null) {
                    logError("onSdkStatusChanged user == null", TAG)
                    toRegistrationFragment()
                    return
                }
                if (!user.validate()) {
                    logError("onSdkStatusChanged !user.validate()", TAG)
                    toRegistrationFragment()
                    return
                }
                evrotrustSDKHelper.checkUserStatus()
                return
            }

            SdkStatus.SDK_SETUP_ERROR, SdkStatus.CRITICAL_ERROR -> {
                logError("sdkStatus SdkStatus.SDK_SETUP_ERROR, SdkStatus.CRITICAL_ERROR", TAG)
                showErrorState(
                    description = StringSource.Res(
                        evrotrustSDKHelper.errorMessageRes ?: R.string.sdk_error_unknown
                    )
                )
                logout()
                return
            }

            SdkStatus.USER_SETUP_ERROR -> {
                logError("sdkStatus == USER_SETUP_ERROR", TAG)
                toRegistrationFragment()
                return
            }

            SdkStatus.USER_STATUS_READY -> {
                logDebug("sdkStatus USER_STATUS_READY", TAG)
                if (appStatus == AppStatus.NOT_READY) {
                    logError("appStatus not ready", TAG)
                    toRegistrationFragment()
                    return
                }
                if (pinCode == null) {
                    logError("onSdkStatusChanged pinCode == null", TAG)
                    showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
                    toRegistrationFragment()
                    return
                }
                if (!pinCode.validate()) {
                    logError("onSdkStatusChanged  !pinCode.validate()", TAG)
                    showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
                    toRegistrationFragment()
                    return
                }
                if (pinCode.errorTimeCode == null || pinCode.errorStatus == ErrorStatus.NO_TIMEOUT) {
                    logDebug("toEnterCodeFragment", TAG)
                    if (appStatus == AppStatus.READY) {
                        toEnterCodeFragment()
                    } else {
                        toRegistrationFragment()
                    }
                } else {
                    val current = System.currentTimeMillis()
                    val timeOnUnlock =
                        pinCode.errorTimeCode!! + pinCode.errorStatus.timeoutMillis
                    if (current < timeOnUnlock) {
                        logError(
                            "time not expired, toBlockedFragment, current: $current, timeOnUnlock: $timeOnUnlock",
                            TAG
                        )
                        toBlockedFragment()
                    } else {
                        logDebug("time expired, toEnterCodeFragment", TAG)
                        if (appStatus == AppStatus.READY) {
                            toEnterCodeFragment()
                        } else {
                            toRegistrationFragment()
                        }
                    }
                }
            }

            else -> {
                logError("sdkStatus else", TAG)
                showBannerMessage(BannerMessage.error("User status not correct, please go through the registration process"))
                toRegistrationFragment()
            }
        }
    }

    private fun toEnterCodeFragment() {
        logDebug("toEnterCodeFragment", TAG)
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toEnterCodeFlowFragment(), viewModelScope
        )
    }

    private fun toBlockedFragment() {
        logDebug("toBlockedFragment", TAG)
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toEnterCodeFlowFragment(), viewModelScope
        )
    }

}