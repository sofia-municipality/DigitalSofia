/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.settings

import androidx.core.view.isVisible
import com.digital.sofia.databinding.FragmentSettingsBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.SupportBiometricManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.properties.Delegates

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsViewModel>() {

    companion object {
        private const val TAG = "SettingsFragmentTag"
    }

    override fun getViewBinding() = FragmentSettingsBinding.inflate(layoutInflater)

    override val viewModel: SettingsViewModel by viewModel()

    private val biometricManager: SupportBiometricManager by inject()

    private var isBiometricAvailable: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            binding.btnAuthMethod.isVisible = newValue
            attachAuthButtonClickListener()
            subscribeToAuthMethodDescriptionLiveData()
        }
    }

    override fun onCreated() {
        isBiometricAvailable = biometricManager.hasBiometrics()
        setupView()
        setupControls()
        subscribeToLiveData()
    }

    override fun onResume() {
        isBiometricAvailable = biometricManager.hasBiometrics()
        viewModel.onResume()
        super.onResume()
    }

    override fun setupView() {
        binding.btnAuthMethod.isVisible = isBiometricAvailable
    }

    override fun setupControls() {
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
        binding.btnProfile.clickListener = {
            logDebug("btnProfile clickListener", TAG)
            viewModel.onProfileClicked()
        }
        binding.btnLanguage.clickListener = {
            logDebug("btnLanguage clickListener", TAG)
            viewModel.onLanguageClicked()
        }
        binding.btnChangePin.clickListener = {
            logDebug("btnChangePin clickListener", TAG)
            viewModel.onChangePinClicked()
        }
        binding.btnDeleteProfile.clickListener = {
            logDebug("btnDeleteProfile clickListener", TAG)
            viewModel.onDeleteProfileClicked()
        }
        attachAuthButtonClickListener()
    }

    override fun subscribeToLiveData() {
        viewModel.currentLanguageLiveData.observe(this) {
            binding.btnLanguage.setDescription(it.nameString)
        }
        subscribeToAuthMethodDescriptionLiveData()
    }

    private fun subscribeToAuthMethodDescriptionLiveData () {
        if (isBiometricAvailable) {
            viewModel.authMethodDescriptionResLiveData.observe(this) {
                if (it != null) {
                    binding.btnAuthMethod.setDescription(it)
                }
            }
        }
    }

    private fun attachAuthButtonClickListener() {
        if (isBiometricAvailable) {
            binding.btnAuthMethod.clickListener = {
                logDebug("btnAuthMethod clickListener", TAG)
                viewModel.onAuthMethodClicked()
            }
        }
    }

}