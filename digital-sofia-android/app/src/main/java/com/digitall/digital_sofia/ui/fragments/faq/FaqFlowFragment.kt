package com.digitall.digital_sofia.ui.fragments.faq

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

class FaqFlowFragment : BaseFlowFragment<FragmentFlowContainerBinding, FaqFlowViewModel>() {

    override fun getViewBinding() = FragmentFlowContainerBinding.inflate(layoutInflater)

    override val viewModel: FaqFlowViewModel by viewModel()

    override fun getFlowGraph() = R.navigation.nav_faq

    override fun getStartDestination(): StartDestination {
        return viewModel.getStartDestination()
    }

}