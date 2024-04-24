/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.pin.enter

import com.digital.sofia.databinding.FragmentRegistrationEnterPinBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.pin.enter.BaseEnterPinFragment
import com.digital.sofia.ui.view.CodeKeyboardView
import com.digital.sofia.ui.view.CodeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationEnterPinFragment :
    BaseEnterPinFragment<FragmentRegistrationEnterPinBinding, RegistrationEnterPinViewModel>() {

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

    override fun setupControls() {
        super.setupControls()
        binding.btnForgotPin.onClickThrottle {
            viewModel.onForgotCodeClicked()
        }
    }

}