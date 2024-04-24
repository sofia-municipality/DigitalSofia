package com.digital.sofia.ui.fragments.settings.delete.profile.error

import com.digital.sofia.databinding.FragmentProfileDeleteErrorBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteProfileErrorFragment :
    BaseFragment<FragmentProfileDeleteErrorBinding, DeleteProfileErrorViewModel>() {

    companion object {
        private const val TAG = "DeleteProfileErrorFragmentTag"
    }

    override fun getViewBinding() = FragmentProfileDeleteErrorBinding.inflate(layoutInflater)

    override val viewModel: DeleteProfileErrorViewModel by viewModel()

    override fun setupControls() {
        binding.btnBack.onClickThrottle {
            viewModel.onBackPressed()
        }
    }

}