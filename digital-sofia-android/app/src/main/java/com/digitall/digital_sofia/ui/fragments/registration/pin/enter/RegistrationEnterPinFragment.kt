package com.digitall.digital_sofia.ui.fragments.registration.pin.enter

import androidx.appcompat.widget.AppCompatButton
import com.digitall.digital_sofia.databinding.FragmentRegistrationEnterPinBinding
import com.digitall.digital_sofia.ui.fragments.base.pin.enter.BaseEnterCodeFragment
import com.digitall.digital_sofia.ui.view.CodeKeyboardView
import com.digitall.digital_sofia.ui.view.CodeView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationEnterPinFragment :
    BaseEnterCodeFragment<FragmentRegistrationEnterPinBinding, RegistrationEnterPinViewModel>() {

    companion object {
        private const val TAG = "RegistrationEnterPinFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationEnterPinBinding.inflate(layoutInflater)

    override val viewModel: RegistrationEnterPinViewModel by viewModel()

    override val countOfDigits: Int = 6

    override fun getPasscodeView(): CodeView {
        return binding.passcodeView
    }

    override fun getKeyboardView(): CodeKeyboardView {
        return binding.keyboard
    }

    override fun getBtnForgotPassword(): AppCompatButton {
        return binding.btnForgotPin
    }
}