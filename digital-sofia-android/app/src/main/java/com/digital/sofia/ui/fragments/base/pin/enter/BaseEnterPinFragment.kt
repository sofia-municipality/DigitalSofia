/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.pin.enter

import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.digital.sofia.R
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.models.common.InputLockState
import com.digital.sofia.models.common.LockState
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.ui.view.CodeKeyboardView
import com.digital.sofia.ui.view.CodeView
import com.digital.sofia.utils.SupportBiometricManager
import org.koin.android.ext.android.inject
import javax.crypto.Cipher

abstract class BaseEnterPinFragment<VB : ViewBinding, VM : BaseEnterPinViewModel> :
    BaseFragment<VB, VM>() {

    companion object {
        private const val TAG = "BaseEnterPinFragmentTag"
    }

    private val biometricManager: SupportBiometricManager by inject()

    protected abstract fun getPasscodeView(): CodeView

    protected abstract fun getKeyboardView(): CodeKeyboardView

    protected abstract val countOfDigits: Int

    @CallSuper
    override fun setupView() {
        logDebug("setupView", TAG)
        getPasscodeView().itemCount = countOfDigits
        getKeyboardView().setMaxNumbersLimit(countOfDigits)
    }

    @CallSuper
    override fun setupControls() {
        logDebug("setupControls", TAG)
        getKeyboardView().onTextChangedCallback = { getPasscodeView().setText(it) }
        getKeyboardView().onFingerprintCallback = { viewModel.startBiometricAuth() }
        getPasscodeView().setCodeReadyListener { viewModel.onPinCompleted(it) }
    }

    @CallSuper
    override fun subscribeToLiveData() {
        logDebug("subscribeToLiveData", TAG)
        viewModel.clearPassCodeFieldLiveData.observe(viewLifecycleOwner) {
            clearPassCodeField()
        }
        viewModel.shakePassCodeFieldLiveData.observe(viewLifecycleOwner) {
            shakePassCodeField()
        }
        viewModel.keyboardLockStateLiveData.observe(viewLifecycleOwner) {
            setKeyboardLockState(it)
        }
        viewModel.enableBiometricLiveData.observe(viewLifecycleOwner) {
            handleEnableBiometric(it)
        }
        viewModel.startBiometricAuthLiveData.observe(viewLifecycleOwner) {
            handleStartBiometricAuth(it)
        }
        biometricManager.onBiometricErrorLiveData.observe(viewLifecycleOwner) {
            viewModel.onBiometricError()
        }
        biometricManager.onBiometricTooManyAttemptsLiveData.observe(viewLifecycleOwner) {
            viewModel.onBiometricTooManyAttempts()
        }
        biometricManager.onBiometricSuccessLiveData.observe(viewLifecycleOwner) {
            viewModel.onBiometricSuccess(it)
        }
    }

    private fun handleEnableBiometric(result: Boolean) {
        logDebug("handleEnableBiometric result: $result", TAG)
        getKeyboardView().showFingerprintButton(result)
    }

    private fun handleStartBiometricAuth(cipher: Cipher) {
        logDebug("handleStartBiometricAuth", TAG)
        try {
            biometricManager.authenticate(cipher)
        } catch (e: Exception) {
            logError("handleStartBiometricAuth exception: ${e.message}", e, TAG)
            showMessage(Message.error(R.string.error_biometric))
            viewModel.onBiometricException()
        }
    }

    private fun setKeyboardLockState(inputLockState: InputLockState) {
        logDebug("setKeyboardLockState inputLockState: $inputLockState", TAG)
        when (inputLockState.state) {
            LockState.LOCKED -> getKeyboardView().lockKeyboard()
            LockState.UNLOCKED -> getKeyboardView().unlockKeyboard()
        }
    }

    private fun clearPassCodeField() {
        logDebug("clearPassCodeField", TAG)
        getPasscodeView().setText("")
        getKeyboardView().clearKeyboard()
    }

    private fun shakePassCodeField() {
        logDebug("shakePassCodeField", TAG)
        getPasscodeView().post { getPasscodeView().shake() }
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        biometricManager.cancelAuthentication()
    }

}