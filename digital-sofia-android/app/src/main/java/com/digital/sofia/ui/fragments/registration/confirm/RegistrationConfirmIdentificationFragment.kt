/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.confirm

import com.digital.sofia.databinding.FragmentRegistrationIntroBinding
import com.digital.sofia.ui.fragments.base.registration.identification.BaseConfirmIdentificationFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationConfirmIdentificationFragment :
    BaseConfirmIdentificationFragment<RegistrationConfirmIdentificationViewModel>() {

    companion object {
        private const val TAG = "RegistrationConfirmIdentificationFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationIntroBinding.inflate(layoutInflater)

    override val viewModel: RegistrationConfirmIdentificationViewModel by viewModel()

    override val prefillPersonalIdentificationNumber = true

}