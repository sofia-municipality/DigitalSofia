/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.registration.identification

import androidx.annotation.StringRes
import com.digital.sofia.R
import com.digital.sofia.domain.models.common.SdkStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.EvrotrustSDKHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.UpdateDocumentsHelper

abstract class BaseConfirmIdentificationViewModel(
    private val preferences: PreferencesRepository,
    private val evrotrustSDKHelper: EvrotrustSDKHelper,
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
) : BaseViewModel(
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
        private const val TAG = "RegistrationConfirmIdentificationViewModelTag"
    }

    final override val isAuthorizationActive: Boolean = false

    internal abstract fun onNoClicked()

    protected abstract fun proceedNext()

    protected abstract fun toErrorFragment()

    protected abstract fun toErrorFragment(@StringRes errorMessageRes: Int)

    internal fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.USER_STATUS_NOT_IDENTIFIED -> {
                showMessage(
                    Message(
                        title = StringSource.Res(R.string.oops_with_dots),
                        message = StringSource.Text(
                            "User information is incomplete, you must update your user information to continue"
                        ),
                        type = Message.Type.ALERT,
                        positiveButtonText = StringSource.Res(R.string.ok),
                        negativeButtonText = StringSource.Res(R.string.cancel),
                    )
                )
            }

            SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_READY -> {
                val pinCode = preferences.readPinCode()
                if (pinCode == null) {
                    logError("onSdkStatusChanged pinCode == null", TAG)
                    toErrorFragment(R.string.error_pin_code_not_setup)
                    return
                }
                if (!pinCode.validate()) {
                    logError("onSdkStatusChanged !pinCode.validate()", TAG)
                    toErrorFragment(R.string.error_pin_code_not_setup)
                    return
                }
                val user = preferences.readUser()
                if (user == null) {
                    logError("onSdkStatusChanged user == null || !user.validate()", TAG)
                    toErrorFragment(R.string.error_user_not_setup_correct)
                    return
                }
                if (!user.validate()) {
                    logError("onCodeLocalCheckSuccess !user.validate())", TAG)
                    toErrorFragment(R.string.error_user_not_setup_correct)
                    return
                }
                proceedNext()
            }

            SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_CANCELLED -> {
                toErrorFragment()
            }

            else -> {
                toErrorFragment(evrotrustSDKHelper.errorMessageRes ?: R.string.sdk_error_unknown)
            }
        }
    }

}