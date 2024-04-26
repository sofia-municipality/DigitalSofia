package com.digital.sofia.ui.fragments.forgot.error

import androidx.navigation.fragment.navArgs
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.ui.fragments.base.registration.error.BaseRegistrationErrorFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPinRegistrationErrorFragment :
    BaseRegistrationErrorFragment<ForgotPinRegistrationErrorViewModel>() {

    companion object {
        private const val TAG = "ForgotPinRegistrationErrorFragmentTag"
    }

    override fun onNextClicked() {
        evrotrustSDKHelper.startSetupUserActivity(
            activity = requireActivity(),
            prefillPersonalIdentificationNumber = false,
        )
    }

    override val viewModel: ForgotPinRegistrationErrorViewModel by viewModel()

    private val args: ForgotPinRegistrationErrorFragmentArgs by navArgs()

    override fun setupView() {
        try {
            val errorMessageRes = args.errorMessage
            binding.tvDescription.text = errorMessageRes.getString(requireContext())
        } catch (e: IllegalStateException) {
            logError("get args Exception: ${e.message}", e, TAG)
        }
    }

}