package com.digital.sofia.ui.fragments.forgot

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentFlowContainerBinding
import com.digital.sofia.models.common.StartDestination
import com.digital.sofia.ui.fragments.base.BaseFlowFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPinRegistrationFlowFragment :
    BaseFlowFragment<FragmentFlowContainerBinding, ForgotPinRegistrationFlowViewModel>() {

    companion object {
        private const val TAG = "ForgotPinRegistrationFlowFragmentTag"
    }

    override fun getViewBinding() = FragmentFlowContainerBinding.inflate(layoutInflater)

    override val viewModel: ForgotPinRegistrationFlowViewModel by viewModel()

    override fun getFlowGraph() = R.navigation.nav_forgot_pin_registration

    override fun getStartDestination(): StartDestination {
        return viewModel.getStartDestination()
    }

}