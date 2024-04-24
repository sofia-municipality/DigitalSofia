/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.pin.biometric

import com.digital.sofia.ui.fragments.base.biometric.BaseEnableBiometricFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePinEnableBiometricFragment :
    BaseEnableBiometricFragment<ChangePinEnableBiometricViewModel>() {

    companion object {
        private const val TAG = "ChangePinEnableBiometricFragmentTag"
    }

    override val viewModel: ChangePinEnableBiometricViewModel by viewModel()

}