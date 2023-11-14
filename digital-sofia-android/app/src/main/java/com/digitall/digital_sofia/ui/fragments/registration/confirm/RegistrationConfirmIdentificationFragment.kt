package com.digitall.digital_sofia.ui.fragments.registration.confirm

import com.digitall.digital_sofia.databinding.FragmentRegistrationIntroBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationConfirmIdentificationFragment :
    BaseFragment<FragmentRegistrationIntroBinding, RegistrationConfirmIdentificationViewModel>() {

    companion object {
        private const val TAG = "RegistrationConfirmIdentificationFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationIntroBinding.inflate(layoutInflater)

    override val viewModel: RegistrationConfirmIdentificationViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override fun setupControls() {
        binding.btnNo.onClickThrottle {
            logDebug("btnNo onClickThrottle", TAG)
            viewModel.onNoClicked()
        }
        binding.btnYes.onClickThrottle {
            logDebug("btnYes onClickThrottle", TAG)
            evrotrustSDKHelper.startSetupUserActivity(requireActivity())
        }
    }

    override fun subscribeToLiveData() {
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            viewModel.onSdkStatusChanged(it)
        }
    }
}