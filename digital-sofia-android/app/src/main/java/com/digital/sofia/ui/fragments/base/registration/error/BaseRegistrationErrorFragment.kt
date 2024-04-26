/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.registration.error

import com.digital.sofia.databinding.FragmentRegistrationErrorBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.models.common.AlertDialogResult
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject

abstract class BaseRegistrationErrorFragment<VM : BaseRegistrationErrorViewModel> :
    BaseFragment<FragmentRegistrationErrorBinding, VM>() {

    companion object {
        private const val TAG = "RegistrationErrorFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationErrorBinding.inflate(layoutInflater)

    protected val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    protected abstract fun onNextClicked()

    override fun onAlertDialogResult(result: AlertDialogResult) {
        if (result.isPositive) {
            logDebug("onAlertDialogResult isPositive", TAG)
            evrotrustSDKHelper.openSettingsScreens(requireActivity())
        } else {
            logDebug("onAlertDialogResult negative", TAG)
            evrotrustSDKHelper.checkUserStatus()
        }
    }

    override fun setupControls() {
        binding.btnNext.onClickThrottle {
            logDebug("btnRetry onClickThrottle", TAG)
            onNextClicked()
        }
        binding.btnBack.onClickThrottle {
            logDebug("btnRetry onClickThrottle", TAG)
            viewModel.onBackPressed()
        }
    }

    override fun subscribeToLiveData() {
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            viewModel.onSdkStatusChanged(it)
        }
        evrotrustSDKHelper.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                binding.tvDescription.text = getString(it)
            }
        }
        viewModel.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                binding.tvDescription.text = getString(it)
            }
        }
    }

}