package com.digital.sofia.ui.fragments.profile.verification.wait

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.common.SdkStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.usecase.user.SubscribeForUserStatusChangeUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.extensions.isFragmentInBackStack
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.popBackStackToFragment
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.NotificationModel
import com.digital.sofia.models.common.ProfileVerificationType
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.EvrotrustSDKHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SingleLiveEvent

class ProfileVerificationWaitViewModel(
    private val preferences: PreferencesRepository,
    private val evrotrustSDKHelper: EvrotrustSDKHelper,
    private val subscribeForUserStatusChangeUseCase: SubscribeForUserStatusChangeUseCase,
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
        private const val TAG = "ProfileVerificationWaitViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    private val _profileVerificationTypeLiveData = SingleLiveEvent<ProfileVerificationType>()
    val profileVerificationTypeLiveData = _profileVerificationTypeLiveData.readOnly()

    override fun onBackPressed() {
        closeActivity()
    }

    fun subscribeForProfileChanges() {
        evrotrustSDKHelper.subscribeForProfileChanges()
    }

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        when (sdkStatus) {
            SdkStatus.USER_PROFILE_VERIFIED -> {
                _profileVerificationTypeLiveData.setValueOnMainThread(
                    ProfileVerificationType.ProfileVerificationReady
                )
                close()
            }

            SdkStatus.USER_PROFILE_IS_SUPERVISED,
            SdkStatus.USER_PROFILE_PROCESSING -> subscribeForUserStatusChange()

            SdkStatus.USER_PROFILE_REJECTED -> {
                _profileVerificationTypeLiveData.setValueOnMainThread(
                    ProfileVerificationType.ProfileVerificationRejected
                )
                close()
            }

            SdkStatus.USER_PROFILE_NOT_VERIFIED -> {}

            else -> {
                _profileVerificationTypeLiveData.setValueOnMainThread(
                    ProfileVerificationType.ProfileVerificationError(
                        errorMessageRes = evrotrustSDKHelper.errorMessageRes
                            ?: R.string.sdk_error_unknown
                    )
                )
                close()
            }
        }
    }

    private fun subscribeForUserStatusChange() {
        val identificationNumber = preferences.readUser()?.personalIdentificationNumber
        subscribeForUserStatusChangeUseCase.invoke(identificationNumber = identificationNumber)
            .launchInScope(viewModelScope)
    }

    private fun close() {
        when {
            findFlowNavController().isFragmentInBackStack(R.id.registrationConfirmIdentificationFragment) -> findFlowNavController().popBackStackToFragment(
                R.id.registrationConfirmIdentificationFragment,
                viewModelScope
            )

            findFlowNavController().isFragmentInBackStack(R.id.forgotPinRegistrationConfirmIdentificationFragment) -> findFlowNavController().popBackStackToFragment(
                R.id.forgotPinRegistrationConfirmIdentificationFragment,
                viewModelScope
            )

            findFlowNavController().isFragmentInBackStack(R.id.splashFragment) -> findFlowNavController().popBackStackToFragment(
                R.id.splashFragment,
                viewModelScope
            )

            findFlowNavController().isFragmentInBackStack(R.id.homeFragment) -> findFlowNavController().popBackStackToFragment(
                R.id.homeFragment,
                viewModelScope
            )
        }
    }

    override fun onNewUserProfileStatusChangeEvent(notificationModel: NotificationModel?) {
        when (notificationModel) {
            is NotificationModel.UserProfileStatusChangeNotificationModel -> {
                clearNewUserProfileStatusChangeEvent()
                when {
                    notificationModel.isIdentified == true && notificationModel.isReadyToSign == true && notificationModel.isRejected == false -> {
                        _profileVerificationTypeLiveData.setValueOnMainThread(
                            ProfileVerificationType.ProfileVerificationReady
                        )
                        close()
                    }

                    notificationModel.isRejected == true -> {
                        _profileVerificationTypeLiveData.setValueOnMainThread(
                            ProfileVerificationType.ProfileVerificationRejected
                        )
                        close()
                    }

                    else -> {
                        _profileVerificationTypeLiveData.setValueOnMainThread(
                            ProfileVerificationType.ProfileVerificationError(
                                errorMessageRes = R.string.sdk_error_user_not_verified
                            )
                        )
                        close()
                    }
                }
            }

            else -> {}
        }
    }
}