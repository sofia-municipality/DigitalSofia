/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.auth.biometric

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentEnterBiometricBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.SupportBiometricManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import javax.crypto.Cipher

class AuthEnterBiometricFragment :
    BaseFragment<FragmentEnterBiometricBinding, AuthEnterBiometricViewModel>() {

    companion object {
        private const val TAG = "EnterBiometricFragmentTag"
    }

    override fun getViewBinding() = FragmentEnterBiometricBinding.inflate(layoutInflater)

    override val viewModel: AuthEnterBiometricViewModel by viewModel()

    private val biometricManager: SupportBiometricManager by inject()

    override fun onResume() {
        super.onResume()
        if (!biometricManager.readyToBiometricAuth()) {
            viewModel.disableBiometric()
            viewModel.toEnterPinFragmentOnDisabledBiometrics()
        }
    }

    override fun setupControls() {
        binding.btnUsePin.onClickThrottle {
            logDebug("btnUsePin onClickThrottle", TAG)
            viewModel.toEnterPinFragment(false)
        }
        binding.icFingerprint.onClickThrottle {
            logDebug("icFingerprint onClickThrottle", TAG)
            viewModel.startBiometricAuth()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.userNameLiveData.observe(this) {
            binding.tvTitle.text = getString(R.string.auth_enter_pin_title, it)
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

    private fun handleStartBiometricAuth(cipher: Cipher) {
        logDebug("handleStartBiometricAuth", TAG)
        try {
            biometricManager.authenticate(cipher)
        } catch (e: Exception) {
            logError("handleStartBiometricAuth exception: ${e.message}", e, TAG)
            viewModel.onBiometricException()
        }
    }

    override fun onStop() {
        super.onStop()
        biometricManager.cancelAuthentication()
    }

}