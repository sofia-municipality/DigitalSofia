package com.digitall.digital_sofia.ui.fragments.registration

import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.FragmentFlowContainerBinding
import com.digitall.digital_sofia.models.common.StartDestination
import com.digitall.digital_sofia.ui.fragments.base.BaseFlowFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationFlowFragment :
    BaseFlowFragment<FragmentFlowContainerBinding, RegistrationFlowViewModel>() {

    companion object {
        private const val TAG = "RegistrationFlowFragmentTag"
    }

    override fun getViewBinding() = FragmentFlowContainerBinding.inflate(layoutInflater)

    override val viewModel: RegistrationFlowViewModel by viewModel()

    override fun getFlowGraph() = R.navigation.nav_auth_registration

    override fun getStartDestination(): StartDestination {
        return viewModel.getStartDestination()
    }

}