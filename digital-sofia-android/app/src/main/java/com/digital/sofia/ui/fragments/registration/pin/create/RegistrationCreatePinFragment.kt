/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.pin.create

import com.digital.sofia.ui.fragments.base.registration.pin.BaseRegistrationCreatePinFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationCreatePinFragment :
    BaseRegistrationCreatePinFragment<RegistrationCreatePinViewModel>() {

    companion object {
        private const val TAG = "RegistrationCreatePinFragmentTag"
    }

    override val viewModel: RegistrationCreatePinViewModel by viewModel()

}