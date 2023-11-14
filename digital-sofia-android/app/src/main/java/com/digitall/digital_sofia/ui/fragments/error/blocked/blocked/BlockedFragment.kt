package com.digitall.digital_sofia.ui.fragments.error.blocked.blocked

import com.digitall.digital_sofia.databinding.FragmentBlockedBinding
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class BlockedFragment :
    BaseFragment<FragmentBlockedBinding, BlockedViewModel>() {

    companion object {
        private const val TAG = "BlockedFragmentTag"
    }

    override fun getViewBinding() = FragmentBlockedBinding.inflate(layoutInflater)

    override val viewModel: BlockedViewModel by viewModel()

    override fun subscribeToLiveData() {
        viewModel.blockedText.observe(this) {
            binding.tvErrorDescription.text = it
        }
    }

}