package com.digitall.digital_sofia.ui.fragments.registration.start

import com.digitall.digital_sofia.databinding.FragmentRegistrationStartBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

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
    }
}