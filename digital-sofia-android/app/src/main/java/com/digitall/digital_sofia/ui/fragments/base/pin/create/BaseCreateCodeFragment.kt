package com.digitall.digital_sofia.ui.fragments.base.pin.create

import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.CreatePinScreenStates
import com.digitall.digital_sofia.models.common.InputLockState
import com.digitall.digital_sofia.models.common.LockState
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.ui.view.CodeKeyboardView
import com.digitall.digital_sofia.ui.view.CodeView

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseCreateCodeFragment<VB : ViewBinding, VM : BaseCreateCodeViewModel> :
    BaseFragment<VB, VM>() {

    companion object {
        private const val TAG = "BaseCreateCodeFragmentTag"
    }

    protected abstract fun setEnterPassCodeState()

    protected abstract fun setConfirmPassCodeState()

    protected abstract val countOfDigits: Int

    protected abstract fun getPasscodeView(): CodeView

    protected abstract fun getPassCodeKeyboardView(): CodeKeyboardView

    @CallSuper
    override fun setupView() {
        getPasscodeView().itemCount = countOfDigits
        getPassCodeKeyboardView().setMaxNumbersLimit(countOfDigits)
    }

    @CallSuper
    override fun setupControls() {
        getPassCodeKeyboardView().onTextChangedCallback = {
            getPasscodeView().setText(it)
        }
        getPasscodeView().setCodeReadyListener {
            viewModel.validatePin(it)
        }
    }

    @CallSuper
    override fun subscribeToLiveData() {
        viewModel.screenStateLiveData.observe(viewLifecycleOwner) {
            setScreenState(it)
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            showBannerMessage(BannerMessage.error(it))
        }
        viewModel.clearPassCodeFieldLiveData.observe(viewLifecycleOwner) {
            clearPassCodeField()
        }
        viewModel.shakePassCodeFieldLiveData.observe(viewLifecycleOwner) {
            shakePassCodeField()
        }
        viewModel.keyboardLockStateLiveData.observe(viewLifecycleOwner) {
            setKeyboardLockState(it)
        }
        viewModel.passCodesDoNotMatchLiveData.observe(viewLifecycleOwner) {
            showBannerMessage(
                BannerMessage.error(getString(R.string.confirm_pin_error_password_not_match))
            )
        }
    }

    private fun setScreenState(screenState: CreatePinScreenStates?) {
        logDebug("setScreenState screenState: $screenState", TAG)
        screenState?.let {
            when (screenState) {
                CreatePinScreenStates.ENTER -> setEnterPassCodeState()
                CreatePinScreenStates.CONFIRM -> setConfirmPassCodeState()
            }
        }
    }

    private fun setKeyboardLockState(inputLockState: InputLockState) {
        logDebug("setKeyboardLockState", TAG)
        when (inputLockState.state) {
            LockState.LOCKED -> getPassCodeKeyboardView().lockKeyboard()
            LockState.UNLOCKED -> getPassCodeKeyboardView().unlockKeyboard()
        }
    }

    private fun clearPassCodeField() {
        logDebug("clearPassCodeField", TAG)
        getPasscodeView().setText("")
        getPassCodeKeyboardView().clearKeyboard()
    }

    private fun shakePassCodeField() {
        logDebug("shakePassCodeField", TAG)
        getPasscodeView().post { getPasscodeView().shake() }
    }
}