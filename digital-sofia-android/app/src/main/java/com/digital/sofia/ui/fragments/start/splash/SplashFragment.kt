/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.start.splash

import com.digital.sofia.databinding.FragmentSplashBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.models.common.AlertDialogResult
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment :
    BaseFragment<FragmentSplashBinding, SplashViewModel>() {

    companion object {
        private const val TAG = "SplashFragmentTag"
    }

    override fun getViewBinding() = FragmentSplashBinding.inflate(layoutInflater)

    override val viewModel: SplashViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

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

}