/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.email

import androidx.core.view.isVisible
import com.digital.sofia.data.START_PHONE_NUMBER
import com.digital.sofia.databinding.FragmentRegistrationEnterEmailBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.extensions.setTextChangeListener
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationEnterEmailFragment :
    BaseFragment<FragmentRegistrationEnterEmailBinding, RegistrationEnterEmailViewModel>() {

    companion object {
        private const val TAG = "RegistrationEnterEgnFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationEnterEmailBinding.inflate(layoutInflater)

    override val viewModel: RegistrationEnterEmailViewModel by viewModel()

    override fun setupControls() {
        binding.etEmail.setTextChangeListener {
            logDebug("etEmail setTextChangeListener text: $it", TAG)
            viewModel.setEmail(it.trim())
        }
        binding.etPhone.setTextChangeListener {
            logDebug("etPhone setTextChangeListener text: $it", TAG)
            when {
                it.startsWith(START_PHONE_NUMBER + "0") -> {
                    binding.etPhone.setText(START_PHONE_NUMBER)
                    binding.etPhone.setSelection(4)
                    viewModel.setPhone(START_PHONE_NUMBER)
                }

                it.startsWith(START_PHONE_NUMBER) -> {
                    viewModel.setPhone(it.trim())
                }

                else -> {
                    binding.etPhone.setText(START_PHONE_NUMBER)
                    binding.etPhone.setSelection(4)
                    viewModel.setPhone(START_PHONE_NUMBER)
                }
            }
        }
        binding.btnNext.onClickThrottle {
            logDebug("btnNext onClickThrottle", TAG)
            viewModel.onNextClicked()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.showEMailErrorLiveData.observe(viewLifecycleOwner) {
            binding.tvEmailError.isVisible = it == true
        }
        viewModel.showPhoneErrorLiveData.observe(viewLifecycleOwner) {
            binding.tvPhoneError.isVisible = it == true
        }
    }

}