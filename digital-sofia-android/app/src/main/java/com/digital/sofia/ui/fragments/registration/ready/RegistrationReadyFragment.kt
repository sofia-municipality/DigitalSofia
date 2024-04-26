/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.ready

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentRegistrationReadyBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationReadyFragment :
    BaseFragment<FragmentRegistrationReadyBinding, RegistrationReadyViewModel>() {

    companion object {
        private const val TAG = "RegistrationReadyFragmentTag"
    }

    override val viewModel: RegistrationReadyViewModel by viewModel()

    override fun getViewBinding() = FragmentRegistrationReadyBinding.inflate(layoutInflater)

    override fun setupControls() {
        binding.btnMyProfile.onClickThrottle {
            logDebug("onShowProfileClicked", TAG)
            viewModel.onShowProfileClicked()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.userNameLiveData.observe(this) {
            binding.tvHello.text = getString(R.string.registration_ready_hello, it)
        }
    }

}