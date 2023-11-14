package com.digitall.digital_sofia.ui.fragments.registration.error

import androidx.navigation.fragment.navArgs
import com.digitall.digital_sofia.databinding.FragmentRegistrationErrorBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationErrorFragment :
    BaseFragment<FragmentRegistrationErrorBinding, RegistrationErrorViewModel>() {

    companion object {
        private const val TAG = "RegistrationErrorFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationErrorBinding.inflate(layoutInflater)

    override val viewModel: RegistrationErrorViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    private val args: RegistrationErrorFragmentArgs by navArgs()

    override fun setupView() {
        try {
            val errorMessageRes = args.errorMessage
            binding.tvErrorViewDescription.text = errorMessageRes.getString(requireContext())
        } catch (e: IllegalStateException) {
            logError("get args Exception: ${e.message}", e, TAG)
        }
    }

    override fun setupControls() {
        binding.btnRetry.onClickThrottle {
            logDebug("btnRetry onClickThrottle", TAG)
            evrotrustSDKHelper.startSetupUserActivity(requireActivity())
        }
    }

    override fun subscribeToLiveData() {
        evrotrustSDKHelper.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                binding.tvErrorViewDescription.text = getString(it)
            }
        }
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            viewModel.onSdkStatusChanged(it)
        }
        viewModel.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                binding.tvErrorViewDescription.text = getString(it)
            }
        }
    }

}