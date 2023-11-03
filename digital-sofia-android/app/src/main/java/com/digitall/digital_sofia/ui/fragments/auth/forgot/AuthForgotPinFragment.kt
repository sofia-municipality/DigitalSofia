package com.digitall.digital_sofia.ui.fragments.auth.forgot

import com.digitall.digital_sofia.databinding.FragmentForgotPinBinding
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

class AuthForgotPinFragment :
    BaseFragment<FragmentForgotPinBinding, AuthForgotPinViewModel>() {

    companion object {
        private const val TAG = "ForgotPinFragmentTag"
    }

    override fun getViewBinding() = FragmentForgotPinBinding.inflate(layoutInflater)

    override val viewModel: AuthForgotPinViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override fun setupControls() {
        binding.btnYes.onClickThrottle {
            logDebug("btnYes onClickThrottle", TAG)
            evrotrustSDKHelper.openEditProfile(
                activity = requireActivity(),
            )
        }
        binding.btnNo.onClickThrottle {
            logDebug("btnNo onClickThrottle", TAG)
            viewModel.onNoClicked()
        }
    }
}