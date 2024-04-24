/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.registration.identification

import com.digital.sofia.databinding.FragmentRegistrationIntroBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.models.common.AlertDialogResult
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject

abstract class BaseConfirmIdentificationFragment<VM : BaseConfirmIdentificationViewModel> :
    BaseFragment<FragmentRegistrationIntroBinding, VM>() {

    companion object {
        private const val TAG = "BaseConfirmIdentificationFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationIntroBinding.inflate(layoutInflater)

    protected val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    abstract val prefillPersonalIdentificationNumber: Boolean

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
        binding.btnNo.onClickThrottle {
            logDebug("onNoClicked", TAG)
            viewModel.onNoClicked()
        }
        binding.btnYes.onClickThrottle {
            logDebug("onYesClicked", TAG)
            evrotrustSDKHelper.startSetupUserActivity(
                activity = requireActivity(),
                prefillPersonalIdentificationNumber = prefillPersonalIdentificationNumber,
            )
        }
    }

    override fun subscribeToLiveData() {
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            viewModel.onSdkStatusChanged(it)
        }
    }
}