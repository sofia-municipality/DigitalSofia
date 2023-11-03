package com.digitall.digital_sofia.ui.fragments.auth.pin

import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.navArgs
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.FragmentEnterPinBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.ui.fragments.base.pin.enter.BaseEnterCodeFragment
import com.digitall.digital_sofia.ui.view.CodeKeyboardView
import com.digitall.digital_sofia.ui.view.CodeView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class AuthEnterPinFragment :
    BaseEnterCodeFragment<FragmentEnterPinBinding, AuthEnterPinViewModel>() {

    companion object {
        private const val TAG = "EnterPinFragmentTag"
    }

    override fun getViewBinding() = FragmentEnterPinBinding.inflate(layoutInflater)

    override val viewModel: AuthEnterPinViewModel by viewModel()

    private val args: AuthEnterPinFragmentArgs by navArgs()

    override fun onCreated() {
        try {
            val forceDisableBiometric = args.forceDisableBiometric
            logDebug("forceDisableBiometric: $forceDisableBiometric", TAG)
            viewModel.forceDisableBiometric = forceDisableBiometric
        } catch (e: IllegalStateException) {
            logError("get args Exception: ${e.message}", e, TAG)
        }
        super.onCreated()
    }

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

    override fun subscribeToLiveData() {
        super.subscribeToLiveData()
        viewModel.userNameLiveData.observe(this) {
            binding.tvTitle.text = getString(R.string.auth_enter_pin_title, it)
        }
    }

}