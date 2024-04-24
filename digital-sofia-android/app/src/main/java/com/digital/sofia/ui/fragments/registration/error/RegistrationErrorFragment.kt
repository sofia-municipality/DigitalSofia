/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.error

import androidx.navigation.fragment.navArgs
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.ui.fragments.base.registration.error.BaseRegistrationErrorFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegistrationErrorFragment :
    BaseRegistrationErrorFragment<RegistrationErrorViewModel>() {

    companion object {
        private const val TAG = "RegistrationErrorFragmentTag"
    }

    override fun onNextClicked() {
        evrotrustSDKHelper.startSetupUserActivity(
            activity = requireActivity(),
            prefillPersonalIdentificationNumber = true,
        )
    }

    override val viewModel: RegistrationErrorViewModel by viewModel()

    private val args: RegistrationErrorFragmentArgs by navArgs()

    override fun setupView() {
        try {
            val errorMessageRes = args.errorMessage
            binding.tvDescription.text = errorMessageRes.getString(requireContext())
        } catch (e: IllegalStateException) {
            logError("get args Exception: ${e.message}", e, TAG)
        }
    }

}