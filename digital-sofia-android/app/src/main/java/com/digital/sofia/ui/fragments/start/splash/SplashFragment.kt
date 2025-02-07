/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.start.splash

import androidx.fragment.app.setFragmentResultListener
import com.digital.sofia.data.extensions.getParcelableCompat
import com.digital.sofia.databinding.FragmentSplashBinding
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.models.common.AlertDialogResult
import com.digital.sofia.models.common.ProfileVerificationType
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment :
    BaseFragment<FragmentSplashBinding, SplashViewModel>() {

    companion object {
        private const val TAG = "SplashFragmentTag"
        private const val PROFILE_VERIFICATION_WAIT_REQUEST_KEY =
            "PROFILE_VERIFICATION_WAIT_REQUEST_KEY"
        private const val PROFILE_VERIFICATION_TYPE_KEY = "PROFILE_VERIFICATION_TYPE_KEY"
    }

    override fun getViewBinding() = FragmentSplashBinding.inflate(layoutInflater)

    override val viewModel: SplashViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    private val preferences: PreferencesRepository by inject()

    override fun onCreated() {
        logDebug("onCreated", TAG)
        evrotrustSDKHelper.setupSdk()
    }

    override fun onAlertDialogResult(result: AlertDialogResult) {
        if (result.isPositive) {
            logDebug("onAlertDialogResult isPositive", TAG)
            evrotrustSDKHelper.openEditProfile(requireActivity())
        } else {
            logDebug("onAlertDialogResult negative", TAG)
            evrotrustSDKHelper.checkUserStatus()
        }
    }

    override fun subscribeToLiveData() {
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            logDebug("sdkStatusLiveData status: ${it.name}", TAG)
            viewModel.onSdkStatusChanged(it)
        }
    }

    override fun setupControls() {
        when (val appStatus = preferences.readAppStatus()) {
            AppStatus.PROFILE_VERIFICATION_FORGOTTEN_PIN,
            AppStatus.PROFILE_VERIFICATION_DOCUMENTS,
            AppStatus.PROFILE_VERIFICATION_REGISTRATION,
            AppStatus.REGISTERED -> {
                setFragmentResultListener(PROFILE_VERIFICATION_WAIT_REQUEST_KEY) { _, bundle ->
                    val profileVerificationType =
                        bundle.getParcelableCompat<ProfileVerificationType>(
                            PROFILE_VERIFICATION_TYPE_KEY
                        )
                    profileVerificationType?.let { type ->
                        when (type) {
                            is ProfileVerificationType.ProfileVerificationReady -> viewModel.proceedNext(
                                appStatus = appStatus
                            )

                            is ProfileVerificationType.ProfileVerificationError -> viewModel.handleError(
                                appStatus = appStatus,
                                errorMessageRes = type.errorMessageRes ?: return@let
                            )

                            is ProfileVerificationType.ProfileVerificationRejected -> evrotrustSDKHelper.openEditProfile(
                                requireActivity()
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }

}