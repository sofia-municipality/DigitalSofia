package com.digitall.digital_sofia.ui.fragments.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.FragmentMainTabsFlowContainerBinding
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.ui.fragments.base.BaseFlowFragment
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class MainTabsFlowFragment :
    BaseFlowFragment<FragmentMainTabsFlowContainerBinding, MainTabsFlowViewModel>() {

    companion object {
        private const val TAG = "MainTabsFlowFragmentTag"
    }

    override fun getViewBinding() = FragmentMainTabsFlowContainerBinding.inflate(layoutInflater)

    override val viewModel: MainTabsFlowViewModel by viewModel()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupControls() {
        val host =
            childFragmentManager.findFragmentById(R.id.flowTabsNavigationContainer) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(host.navController)
    }

    override fun subscribeToLiveData() {
        super.subscribeToLiveData()
        viewModel.documentsForSignLiveData.onEach {
            binding.bottomNavigationView.getOrCreateBadge(R.id.nav_main_home).isVisible = it
        }.launch(lifecycleScope)
    }

}