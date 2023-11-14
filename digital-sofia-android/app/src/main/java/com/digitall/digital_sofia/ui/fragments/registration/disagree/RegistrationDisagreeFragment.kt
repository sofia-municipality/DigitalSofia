package com.digitall.digital_sofia.ui.fragments.registration.disagree

import com.digitall.digital_sofia.databinding.FragmentRegistrationDisagreeBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationDisagreeFragment :
    BaseFragment<FragmentRegistrationDisagreeBinding, RegistrationDisagreeViewModel>() {

    companion object {
        private const val TAG = "RegistrationDisagreeFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationDisagreeBinding.inflate(layoutInflater)

    override val viewModel: RegistrationDisagreeViewModel by viewModel()

    override fun setupControls() {
        binding.btnNo.onClickThrottle {
            logDebug("btnNo onClickThrottle", TAG)
            viewModel.onNoClicked()
        }
        binding.btnYes.onClickThrottle {
            logDebug("btnYes onClickThrottle", TAG)
            viewModel.onYesClicked()
        }
    }

}