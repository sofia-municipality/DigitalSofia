/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.registration.pin

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentRegistrationCreatePinBinding
import com.digital.sofia.ui.fragments.base.pin.create.BaseCreatePinFragment
import com.digital.sofia.ui.view.CodeKeyboardView
import com.digital.sofia.ui.view.CodeView

abstract class BaseRegistrationCreatePinFragment<VM : BaseRegistrationCreatePinViewModel> :
    BaseCreatePinFragment<FragmentRegistrationCreatePinBinding, VM>() {

    companion object {
        private const val TAG = "BaseRegistrationCreatePinFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationCreatePinBinding.inflate(layoutInflater)

    final override fun getPasscodeView(): CodeView {
        return binding.passcodeView
    }

    final override fun getPassCodeKeyboardView(): CodeKeyboardView {
        return binding.keyboard
    }

    final override fun setEnterPassCodeState() {
        binding.tvTitle.setText(R.string.registration_create_pin_title)
        binding.tvDescription.setText(R.string.registration_create_pin_description)
    }

    final override fun setConfirmPassCodeState() {
        binding.tvTitle.setText(R.string.registration_create_pin_confirm_title)
        binding.tvDescription.setText(R.string.registration_create_pin_confirm_description)
    }

}