/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.start

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.digital.sofia.databinding.FragmentRegistrationStartBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationStartFragment :
    BaseFragment<FragmentRegistrationStartBinding, RegistrationStartViewModel>() {

    companion object {
        private const val TAG = "RegistrationStartFragmentTag"
    }

    override val viewModel: RegistrationStartViewModel by viewModel()

    override fun getViewBinding() = FragmentRegistrationStartBinding.inflate(layoutInflater)

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun setupControls() {
        binding.btnRegistration.onClickThrottle {
            logDebug("btnRegistration onClickThrottle", TAG)
            viewModel.onRegistrationClicked()
        }
        binding.btnBeta.onClickThrottle {
            logDebug("btnBeta onClickThrottle", TAG)
            viewModel.showBetaState()
        }
    }

    override fun onCreated() {
        super.onCreated()
        requestNotificationPermissions()
    }

    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}