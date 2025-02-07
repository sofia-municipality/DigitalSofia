package com.digital.sofia.ui.fragments.registration.notifications

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.digital.sofia.databinding.FragmentRegistrationNotificationsInformationBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationNotificationsFragment :
    BaseFragment<FragmentRegistrationNotificationsInformationBinding, RegistrationNotificationsViewModel>() {

    companion object {
        private const val TAG = "RegistrationNotificationsFragmentTag"
    }

    override val viewModel: RegistrationNotificationsViewModel by viewModel()

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.proceedNext()
    }

    override fun getViewBinding() =
        FragmentRegistrationNotificationsInformationBinding.inflate(layoutInflater)

    override fun setupControls() {
        binding.btnAgree.onClickThrottle {
            logDebug("btnAgree onClickThrottle", TAG)
            requestNotificationPermissions()
        }
    }

    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}