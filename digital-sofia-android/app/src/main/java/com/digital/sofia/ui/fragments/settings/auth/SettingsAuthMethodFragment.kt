/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.auth

import android.widget.CompoundButton
import com.digital.sofia.databinding.FragmentAuthMethodBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.SupportBiometricManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.properties.Delegates

class SettingsAuthMethodFragment :
    BaseFragment<FragmentAuthMethodBinding, SettingsAuthMethodViewModel>() {

    companion object {
        private const val TAG = "AuthMethodFragmentTag"
    }

    override fun getViewBinding() = FragmentAuthMethodBinding.inflate(layoutInflater)

    override val viewModel: SettingsAuthMethodViewModel by viewModel()

    private val biometricManager: SupportBiometricManager by inject()

    private val switchBiometricListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        isBiometricEnabled = isChecked
    }

    private var isBiometricEnabled: Boolean by Delegates.observable(biometricManager.readyToBiometricAuth()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            viewModel.enableBiometric(newValue)
        }
    }

    override fun setupControls() {
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
        binding.switchBiometric.setOnCheckedChangeListener(switchBiometricListener)
    }

    override fun subscribeToLiveData() {
        viewModel.enableBiometricLiveData.observe(this) {
            binding.switchBiometric.setOnCheckedChangeListener(null)
            binding.switchBiometric.isChecked = it
            binding.switchBiometric.setOnCheckedChangeListener(switchBiometricListener)
        }
        biometricManager.onBiometricErrorLiveData.observe(viewLifecycleOwner) {
            viewModel.onBiometricError()
        }
        biometricManager.onBiometricTooManyAttemptsLiveData.observe(viewLifecycleOwner) {
            viewModel.onBiometricTooManyAttempts()
        }
        biometricManager.onBiometricSuccessLiveData.observe(viewLifecycleOwner) {
            viewModel.confirmFingerprint(it)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!biometricManager.hasBiometrics()) {
            viewModel.enableBiometric(isEnabled = false)
            onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        biometricManager.cancelAuthentication()
    }

}
