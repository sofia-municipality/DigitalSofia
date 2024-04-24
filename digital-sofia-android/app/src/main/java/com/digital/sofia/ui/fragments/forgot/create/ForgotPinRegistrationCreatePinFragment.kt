package com.digital.sofia.ui.fragments.forgot.create

import com.digital.sofia.ui.fragments.base.registration.pin.BaseRegistrationCreatePinFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPinRegistrationCreatePinFragment :
    BaseRegistrationCreatePinFragment<ForgotPinRegistrationCreatePinViewModel>() {

    companion object {
        private const val TAG = "ForgotPinRegistrationCreatePinFragmentTag"
    }

    override val viewModel: ForgotPinRegistrationCreatePinViewModel by viewModel()

}