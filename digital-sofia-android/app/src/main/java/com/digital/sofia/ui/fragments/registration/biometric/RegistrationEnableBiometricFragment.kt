/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.biometric

import com.digital.sofia.ui.fragments.base.biometric.BaseEnableBiometricFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationEnableBiometricFragment :
    BaseEnableBiometricFragment<RegistrationEnableBiometricViewModel>() {

    companion object {
        private const val TAG = "RegistrationEnableBiometricFragmentTag"
    }

    override val viewModel: RegistrationEnableBiometricViewModel by viewModel()

}