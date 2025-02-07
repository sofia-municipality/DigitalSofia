package com.digital.sofia.ui.fragments.profile.verification.wait.flow

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentFlowContainerBinding
import com.digital.sofia.models.common.StartDestination
import com.digital.sofia.ui.fragments.base.BaseFlowFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileVerificationWaitFlowFragment :
    BaseFlowFragment<FragmentFlowContainerBinding, ProfileVerificationWaitFlowViewModel>() {

    override fun getViewBinding() = FragmentFlowContainerBinding.inflate(layoutInflater)

    override val viewModel: ProfileVerificationWaitFlowViewModel by viewModel()

    override fun getFlowGraph() = R.navigation.nav_profile_verification_wait

    override fun getStartDestination(): StartDestination = viewModel.getStartDestination()

}