package com.digitall.digital_sofia.ui.fragments.registration.email

import androidx.core.view.isVisible
import com.digitall.digital_sofia.databinding.FragmentRegistrationEnterEmailBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.extensions.setTextChangeListener
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationEnterEmailFragment :
    BaseFragment<FragmentRegistrationEnterEmailBinding, RegistrationEnterEmailViewModel>() {

    companion object {
        private const val TAG = "RegistrationEnterEgnFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationEnterEmailBinding.inflate(layoutInflater)

    override val viewModel: RegistrationEnterEmailViewModel by viewModel()

    override fun setupControls() {
        binding.etEmail.setTextChangeListener {
            viewModel.setEmail(it.trim())
        }
        binding.etPhone.setTextChangeListener {
            viewModel.setPhone(it.trim())
        }
        binding.btnNext.onClickThrottle {
            logDebug("btnNext onClickThrottle", TAG)
            viewModel.onNextClicked()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.savedEmailLiveData.observe(viewLifecycleOwner) {
            binding.etEmail.setText(it)
        }
        viewModel.savedPhoneLiveData.observe(viewLifecycleOwner) {
            binding.etPhone.setText(it)
        }
        viewModel.showEMailErrorLiveData.observe(viewLifecycleOwner) {
            binding.tvEmailError.isVisible = it == true
        }
        viewModel.showPhoneErrorLiveData.observe(viewLifecycleOwner) {
            binding.tvPhoneError.isVisible = it == true
        }
    }

}