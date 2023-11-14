package com.digitall.digital_sofia.ui.fragments.base.biometric

import androidx.annotation.CallSuper
import com.digitall.digital_sofia.databinding.FragmentEnableBiometricBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.utils.SupportBiometricManager
import org.koin.android.ext.android.inject

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseEnableBiometricFragment<VM : BaseEnableBiometricViewModel> :
    BaseFragment<FragmentEnableBiometricBinding, VM>() {

    companion object {
        private const val TAG = "BaseEnableBiometricFragmentTag"
    }

    override fun getViewBinding() = FragmentEnableBiometricBinding.inflate(layoutInflater)

    private val biometricManager: SupportBiometricManager by inject()

    @CallSuper
    override fun onCreated() {
        logDebug("onCreated", TAG)
        biometricManager.setupBiometricManager(this)
    }

    @CallSuper
    override fun setupControls() {
        binding.btnYes.onClickThrottle {
            logDebug("btnEnable onClickThrottle", TAG)
            viewModel.startBiometric()
        }
        binding.btnNo.onClickThrottle {
            logDebug("btnLater onClickThrottle", TAG)
            viewModel.setupLater()
        }
    }

    @CallSuper
    override fun subscribeToLiveData() {
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

    @CallSuper
    override fun onStop() {
        super.onStop()
        biometricManager.cancelAuthentication()
    }

}