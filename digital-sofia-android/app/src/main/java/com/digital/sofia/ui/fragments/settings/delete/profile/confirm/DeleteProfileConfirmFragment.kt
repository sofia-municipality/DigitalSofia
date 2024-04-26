package com.digital.sofia.ui.fragments.settings.delete.profile.confirm

import com.digital.sofia.databinding.FragmentProfileDeleteConfirmBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteProfileConfirmFragment :
    BaseFragment<FragmentProfileDeleteConfirmBinding, DeleteProfileConfirmViewModel>() {

    companion object {
        private const val TAG = "DeleteProfileConfirmFragmentTag"
    }

    override fun getViewBinding() = FragmentProfileDeleteConfirmBinding.inflate(layoutInflater)

    override val viewModel: DeleteProfileConfirmViewModel by viewModel()

    override fun setupControls() {
        binding.btnDelete.onClickThrottle {
            viewModel.onDeleteClicked()
        }
        binding.btnBack.onClickThrottle {
            viewModel.onBackPressed()
        }
    }

}