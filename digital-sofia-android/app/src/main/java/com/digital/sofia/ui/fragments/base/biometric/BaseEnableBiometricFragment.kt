/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.biometric

import androidx.annotation.CallSuper
import com.digital.sofia.databinding.FragmentEnableBiometricBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.SupportBiometricManager
import org.koin.android.ext.android.inject

abstract class BaseEnableBiometricFragment<VM : BaseEnableBiometricViewModel> :
    BaseFragment<FragmentEnableBiometricBinding, VM>() {

    companion object {
        private const val TAG = "BaseEnableBiometricFragmentTag"
    }

    override fun getViewBinding() = FragmentEnableBiometricBinding.inflate(layoutInflater)

    private val biometricManager: SupportBiometricManager by inject()

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