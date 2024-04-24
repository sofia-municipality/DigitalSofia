/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentMainTabsFlowContainerBinding
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.ui.fragments.base.BaseFlowFragment
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainTabsFlowFragment :
    BaseFlowFragment<FragmentMainTabsFlowContainerBinding, MainTabsFlowViewModel>() {

    companion object {
        private const val TAG = "MainTabsFlowFragmentTag"
        private const val SIGNING_DOCUMENTS = "signing_documents"
        private const val HISTORY_DOCUMENTS = "history_documents"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        activity?.window?.decorView?.setOnApplyWindowInsetsListener { view, insets ->
            val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
            val isImeVisible = insetsCompat.isVisible(WindowInsetsCompat.Type.ime())

            if (isVisible) {
                binding.bottomNavigationView.isVisible = !isImeVisible
            }

            view.onApplyWindowInsets(insets)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getViewBinding() = FragmentMainTabsFlowContainerBinding.inflate(layoutInflater)

    override val viewModel: MainTabsFlowViewModel by viewModel()

    override fun setupControls() {
        val host =
            childFragmentManager.findFragmentById(R.id.flowTabsNavigationContainer) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(host.navController)
    }

    override fun subscribeToLiveData() {
        super.subscribeToLiveData()
        viewModel.documentsForSignLiveData.observe(viewLifecycleOwner) {
            binding.bottomNavigationView.getOrCreateBadge(R.id.nav_main_home).isVisible = it
        }
        viewModel.newSignedDocumentLiveEventNotification.observe(viewLifecycleOwner) {
            handleNavigationToFragment(HISTORY_DOCUMENTS)
        }
        viewModel.newPendingDocumentLiveEventNotification.observe(viewLifecycleOwner) {
            handleNavigationToFragment(SIGNING_DOCUMENTS)
        }
    }

    override fun onCreated() {
        super.onCreated()
        if (Build.VERSION.SDK_INT > 32) {
            val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun handleNavigationToFragment(fragmentName: String) {
        when (fragmentName) {
            HISTORY_DOCUMENTS -> {
                if (binding.bottomNavigationView.selectedItemId != R.id.nav_main_documents) {
                    binding.bottomNavigationView.selectedItemId = R.id.nav_main_documents
                }
            }

            SIGNING_DOCUMENTS -> {
                if (binding.bottomNavigationView.selectedItemId != R.id.nav_main_home) {
                    binding.bottomNavigationView.selectedItemId = R.id.nav_main_home
                }
            }

            else -> {}
        }
    }
}