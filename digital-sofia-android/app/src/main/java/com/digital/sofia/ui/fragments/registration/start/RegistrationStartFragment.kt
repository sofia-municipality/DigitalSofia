/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.start

import com.digital.sofia.databinding.FragmentRegistrationStartBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationStartFragment :
    BaseFragment<FragmentRegistrationStartBinding, RegistrationStartViewModel>() {

    companion object {
        private const val TAG = "SRegistrationStartFragmentTag"
    }

    override val viewModel: RegistrationStartViewModel by viewModel()

    override fun getViewBinding() = FragmentRegistrationStartBinding.inflate(layoutInflater)

    override fun setupControls() {
        binding.btnRegistration.onClickThrottle {
            logDebug("btnRegistration onClickThrottle", TAG)
            viewModel.onRegistrationClicked()
        }
        binding.btnBeta.onClickThrottle {
            logDebug("btnBeta onClickThrottle", TAG)
            viewModel.showBetaState()
        }
    }
}