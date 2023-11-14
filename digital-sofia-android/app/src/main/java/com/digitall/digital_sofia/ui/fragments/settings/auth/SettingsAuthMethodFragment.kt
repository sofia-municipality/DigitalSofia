package com.digitall.digital_sofia.ui.fragments.settings.auth

import android.widget.CompoundButton
import com.digitall.digital_sofia.databinding.FragmentAuthMethodBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.utils.SupportBiometricManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class SettingsAuthMethodFragment :
    BaseFragment<FragmentAuthMethodBinding, SettingsAuthMethodViewModel>() {

    companion object {
        private const val TAG = "AuthMethodFragmentTag"
    }

    override fun getViewBinding() = FragmentAuthMethodBinding.inflate(layoutInflater)

    override val viewModel: SettingsAuthMethodViewModel by viewModel()

    private val biometricManager: SupportBiometricManager by inject()

    private val switchBiometricListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        viewModel.enableBiometric(isChecked)
    }

    override fun onCreated() {
        biometricManager.setupBiometricManager(this)
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

    override fun onStop() {
        super.onStop()
        biometricManager.cancelAuthentication()
    }

}
