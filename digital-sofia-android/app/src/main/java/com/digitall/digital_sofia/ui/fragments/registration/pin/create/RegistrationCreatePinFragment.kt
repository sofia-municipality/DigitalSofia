package com.digitall.digital_sofia.ui.fragments.registration.pin.create

import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.FragmentRegistrationCreatePinBinding
import com.digitall.digital_sofia.ui.fragments.base.pin.create.BaseCreateCodeFragment
import com.digitall.digital_sofia.ui.view.CodeKeyboardView
import com.digitall.digital_sofia.ui.view.CodeView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationCreatePinFragment :
    BaseCreateCodeFragment<FragmentRegistrationCreatePinBinding, RegistrationCreatePinViewModel>() {

    companion object {
        private const val TAG = "RegistrationCreatePinFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationCreatePinBinding.inflate(layoutInflater)

    override val viewModel: RegistrationCreatePinViewModel by viewModel()

    override val countOfDigits: Int = 6

    override fun getPasscodeView(): CodeView {
        return binding.passcodeView
    }

    override fun getPassCodeKeyboardView(): CodeKeyboardView {
        return binding.keyboard
    }

    override fun setEnterPassCodeState() {
        binding.tvTitle.setText(R.string.registration_create_pin_title)
        binding.tvDescription.setText(R.string.registration_create_pin_description)
    }

    override fun setConfirmPassCodeState() {
        binding.tvTitle.setText(R.string.registration_create_pin_confirm_title)
        binding.tvDescription.setText(R.string.registration_create_pin_confirm_description)
    }
}