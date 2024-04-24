package com.digital.sofia.ui.fragments.forgot.biometric

import com.digital.sofia.ui.fragments.base.biometric.BaseEnableBiometricFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPinRegistrationEnableBiometricFragment :
    BaseEnableBiometricFragment<ForgotPinRegistrationEnableBiometricViewModel>() {

    companion object {
        private const val TAG = "ForgotPinRegistrationEnableBiometricFragmentTag"
    }

    override val viewModel: ForgotPinRegistrationEnableBiometricViewModel by viewModel()

}