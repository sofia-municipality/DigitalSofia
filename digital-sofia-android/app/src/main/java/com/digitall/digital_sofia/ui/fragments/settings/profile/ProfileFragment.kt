package com.digitall.digital_sofia.ui.fragments.settings.profile

import com.digitall.digital_sofia.databinding.FragmentProfileBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {

    companion object {
        private const val TAG = "ProfileFragmentTag"
    }

    override fun getViewBinding() = FragmentProfileBinding.inflate(layoutInflater)

    override val viewModel: ProfileViewModel by viewModel()

    override fun setupControls() {
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.userNameLiveData.observe(this) {
            binding.tvUserName.text = it
        }
    }

}