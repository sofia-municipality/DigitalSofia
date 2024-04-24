/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.profile

import com.digital.sofia.databinding.FragmentProfileBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

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