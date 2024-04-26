/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.auth.pin

import androidx.navigation.fragment.navArgs
import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentEnterPinBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.pin.enter.BaseEnterPinFragment
import com.digital.sofia.ui.view.CodeKeyboardView
import com.digital.sofia.ui.view.CodeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthEnterPinFragment :
    BaseEnterPinFragment<FragmentEnterPinBinding, AuthEnterPinViewModel>() {

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

    override fun setupControls() {
        super.setupControls()
        binding.btnForgotPin.onClickThrottle {
            viewModel.onForgotCodeClicked()
        }
    }

    override fun subscribeToLiveData() {
        super.subscribeToLiveData()
        viewModel.userNameLiveData.observe(this) {
            binding.tvTitle.text = getString(R.string.auth_enter_pin_title, it)
        }
    }

}