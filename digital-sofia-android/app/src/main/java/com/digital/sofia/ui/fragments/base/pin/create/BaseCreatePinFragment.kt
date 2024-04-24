/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.pin.create

import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.digital.sofia.R
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.models.common.CreatePinScreenStates
import com.digital.sofia.models.common.InputLockState
import com.digital.sofia.models.common.LockState
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.ui.view.CodeKeyboardView
import com.digital.sofia.ui.view.CodeView

abstract class BaseCreatePinFragment<VB : ViewBinding, VM : BaseCreatePinViewModel> :
    BaseFragment<VB, VM>() {

    companion object {
        private const val TAG = "BaseCreatePinFragmentTag"
    }

    protected abstract fun setEnterPassCodeState()

    protected abstract fun setConfirmPassCodeState()

    private val countOfDigits: Int = 6

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
            showMessage(
                Message(
                    title = StringSource.Res(R.string.information),
                    message = StringSource.Text(it),
                    type = Message.Type.ALERT,
                    positiveButtonText = StringSource.Res(R.string.ok)
                )
            )
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
            showMessage(
                Message(
                    title = StringSource.Res(R.string.information),
                    message = StringSource.Res(R.string.confirm_pin_error_password_not_match),
                    type = Message.Type.ALERT,
                    positiveButtonText = StringSource.Res(R.string.ok)
                )
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