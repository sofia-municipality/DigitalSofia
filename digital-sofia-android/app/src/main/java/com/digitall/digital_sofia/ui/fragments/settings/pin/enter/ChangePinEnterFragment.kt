package com.digitall.digital_sofia.ui.fragments.settings.pin.enter

import androidx.appcompat.widget.AppCompatButton
import com.digitall.digital_sofia.databinding.FragmentChangePinEnterBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.ui.fragments.base.pin.enter.BaseEnterCodeFragment
import com.digitall.digital_sofia.ui.view.CodeKeyboardView
import com.digitall.digital_sofia.ui.view.CodeView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ChangePinEnterFragment :
    BaseEnterCodeFragment<FragmentChangePinEnterBinding, ChangePinEnterViewModel>() {

    companion object {
        private const val TAG = "ChangePinEnterFragmentTag"
    }

    override fun getViewBinding() = FragmentChangePinEnterBinding.inflate(layoutInflater)

    override val viewModel: ChangePinEnterViewModel by viewModel()

    override fun getPasscodeView(): CodeView {
        return binding.passcodeView
    }

    override fun getKeyboardView(): CodeKeyboardView {
        return binding.keyboard
    }

    override fun getBtnForgotPassword(): AppCompatButton? {
        return null
    }

    override val countOfDigits: Int = 6

    override fun setupControls() {
        super.setupControls()
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
    }

}