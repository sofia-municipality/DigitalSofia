/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.confirmation

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentFlowContainerBinding
import com.digital.sofia.models.common.StartDestination
import com.digital.sofia.ui.fragments.base.BaseFlowFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmationFlowFragment :
    BaseFlowFragment<FragmentFlowContainerBinding, ConfirmationFlowViewModel>() {

    companion object {
        private const val TAG = "ConfirmationFlowFragmentTag"
    }

    override fun getViewBinding() = FragmentFlowContainerBinding.inflate(layoutInflater)

    override val viewModel: ConfirmationFlowViewModel by viewModel()

    override fun getFlowGraph() = R.navigation.nav_confirmation

    override fun getStartDestination(): StartDestination {
        return viewModel.getStartDestination()
    }

}