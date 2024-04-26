/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.disagree

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentRegistrationDisagreeBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPinDisagreeFragment :
    BaseFragment<FragmentRegistrationDisagreeBinding, RegistrationDisagreeViewModel>() {

    companion object {
        private const val TAG = "ForgotPinDisagreeFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationDisagreeBinding.inflate(layoutInflater)

    override val viewModel: RegistrationDisagreeViewModel by viewModel()

    override fun subscribeToLiveData() {
        viewModel.userNameLiveData.observe(this) {
            binding.tvTitle.text = getString(R.string.auth_enter_pin_title, it)
        }
    }

    override fun setupControls() {
        binding.btnNext.onClickThrottle {
            logDebug("btnNo onClickThrottle", TAG)
            viewModel.onNextClicked()
        }
    }

}