package com.digital.sofia.ui.fragments.profile.verification.wait

import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import com.digital.sofia.databinding.FragmentProfileVerificationWaitBinding
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileVerificationWaitFragment :
    BaseFragment<FragmentProfileVerificationWaitBinding, ProfileVerificationWaitViewModel>() {

    companion object {
        private const val TAG = "ProfileVerificationWaitFragmentTag"
        private const val PROFILE_VERIFICATION_WAIT_REQUEST_KEY =
            "PROFILE_VERIFICATION_WAIT_REQUEST_KEY"
        private const val PROFILE_VERIFICATION_TYPE_KEY = "PROFILE_VERIFICATION_TYPE_KEY"
    }

    override fun getViewBinding() = FragmentProfileVerificationWaitBinding.inflate(layoutInflater)

    override val viewModel: ProfileVerificationWaitViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override fun onCreated() {
        viewModel.subscribeForProfileChanges()
    }

    override fun subscribeToLiveData() {
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) { status ->
            viewModel.onSdkStatusChanged(sdkStatus = status)
        }

        viewModel.profileVerificationTypeLiveData.observe(viewLifecycleOwner) { type ->
            val bundle = Bundle().apply {
                putParcelable(PROFILE_VERIFICATION_TYPE_KEY, type)
            }
            setFragmentResult(PROFILE_VERIFICATION_WAIT_REQUEST_KEY, bundle)
        }
    }

}