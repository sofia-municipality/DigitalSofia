package com.digitall.digital_sofia.ui.fragments.settings.settings

import androidx.core.view.isVisible
import com.digitall.digital_sofia.databinding.FragmentSettingsBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.utils.SupportBiometricManager
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsViewModel>() {

    companion object {
        private const val TAG = "SettingsFragmentTag"
    }

    override fun getViewBinding() = FragmentSettingsBinding.inflate(layoutInflater)

    override val viewModel: SettingsViewModel by viewModel()

    private var isBiometricAvailable = false

    override fun onCreated() {
        isBiometricAvailable = SupportBiometricManager.hasBiometrics(requireContext())
        setupView()
        setupControls()
        subscribeToLiveData()
    }

    override fun onResume() {
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
        if (isBiometricAvailable) {
            binding.btnAuthMethod.clickListener = {
            logDebug("btnAuthMethod clickListener", TAG)
                viewModel.onAuthMethodClicked()
            }
        }
        binding.btnChangePin.clickListener = {
            logDebug("btnChangePin clickListener", TAG)
            viewModel.onChangePinClicked()
        }
        binding.btnDeleteProfile.clickListener = {
            logDebug("btnDeleteProfile clickListener", TAG)
            viewModel.onDeleteProfileClicked()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.currentLanguageLiveData.observe(this) {
            binding.btnLanguage.setDescription(it.nameString)
        }
        if (isBiometricAvailable) {
            viewModel.authMethodDescriptionRes.observe(this) {
                if (it != null) {
                    binding.btnAuthMethod.setDescription(it)
                }
            }
        }
    }

}