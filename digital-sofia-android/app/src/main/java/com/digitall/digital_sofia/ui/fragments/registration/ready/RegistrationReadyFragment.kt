package com.digitall.digital_sofia.ui.fragments.registration.ready

import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.FragmentRegistrationReadyBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationReadyFragment :
    BaseFragment<FragmentRegistrationReadyBinding, RegistrationReadyViewModel>() {

    companion object {
        private const val TAG = "RegistrationReadyFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationReadyBinding.inflate(layoutInflater)

    override val viewModel: RegistrationReadyViewModel by viewModel()

    override fun setupControls() {
        binding.btnMyProfile.onClickThrottle {
            logDebug("btnMyProfile onClickThrottle", TAG)
            viewModel.onShowProfileClicked()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.userNameLiveData.observe(this) {
            binding.tvHello.text = getString(R.string.registration_ready_hello, it)
        }
    }

}