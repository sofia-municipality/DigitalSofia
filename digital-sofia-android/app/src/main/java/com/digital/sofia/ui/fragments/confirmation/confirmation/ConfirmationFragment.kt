/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.confirmation.confirmation

import com.digital.sofia.databinding.FragmentConfirmationBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmationFragment :
    BaseFragment<FragmentConfirmationBinding, ConfirmationViewModel>() {

    companion object {
        private const val TAG = "ConfirmationFragmentTag"
    }

    override fun getViewBinding() = FragmentConfirmationBinding.inflate(layoutInflater)

    override val viewModel: ConfirmationViewModel by viewModel()

    override fun setupControls() {
        binding.btnYes.onClickThrottle {
            viewModel.onYesClicked()
        }
        binding.btnNo.onClickThrottle {
            viewModel.onNoClicked()
        }
    }

}