package com.digital.sofia.ui.fragments.forgot.confirm

import com.digital.sofia.databinding.FragmentRegistrationIntroBinding
import com.digital.sofia.ui.fragments.base.registration.identification.BaseConfirmIdentificationFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPinRegistrationConfirmIdentificationFragment :
    BaseConfirmIdentificationFragment<ForgotPinRegistrationConfirmIdentificationViewModel>() {

    companion object {
        private const val TAG = "ForgotPinRegistrationConfirmIdentificationFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationIntroBinding.inflate(layoutInflater)

    override val viewModel: ForgotPinRegistrationConfirmIdentificationViewModel by viewModel()

    override val prefillPersonalIdentificationNumber = false

}