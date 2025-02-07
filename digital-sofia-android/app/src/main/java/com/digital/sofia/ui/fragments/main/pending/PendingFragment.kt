/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.pending

import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import com.digital.sofia.R
import com.digital.sofia.data.extensions.getParcelableCompat
import com.digital.sofia.databinding.FragmentHomeBinding
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.enableChangeAnimations
import com.digital.sofia.extensions.registerChangeStateObserver
import com.digital.sofia.extensions.unregisterChangeStateObserver
import com.digital.sofia.models.common.AlertDialogResult
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.ProfileVerificationType
import com.digital.sofia.models.forms.PendingAdapterMarker
import com.digital.sofia.models.forms.PendingDocumentUi
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.ui.fragments.main.pending.list.PendingAdapter
import com.digital.sofia.utils.EvrotrustSDKHelper
import com.digital.sofia.utils.RecyclerViewAdapterDataObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PendingFragment :
    BaseFragment<FragmentHomeBinding, PendingViewModel>(),
    PendingAdapter.ClickListener {

    companion object {
        private const val TAG = "PendingFragmentTag"
        private const val DELAY_500 = 500L
        private const val PROFILE_VERIFICATION_WAIT_REQUEST_KEY =
            "PROFILE_VERIFICATION_WAIT_REQUEST_KEY"
        private const val PROFILE_VERIFICATION_TYPE_KEY = "PROFILE_VERIFICATION_TYPE_KEY"
    }

    override val viewModel: PendingViewModel by viewModel()

    private val adapter: PendingAdapter by inject()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    private val preferences: PreferencesRepository by inject()

    private val adapterDataObserver: RecyclerViewAdapterDataObserver by inject()

    private var updateEmptyStateJob: Job? = null

    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)

    override fun onCreated() {
        super.onCreated()
        viewModel.clearNewPendingDocumentEvent()
    }

    override fun setupView() {
        binding.rvDocuments.adapter = adapter
        adapter.clickListener = this
        binding.rvDocuments.enableChangeAnimations(false)
        binding.rvDocuments.isNestedScrollingEnabled = false
    }

    override fun setupControls() {
        binding.refreshLayout.setOnRefreshListener(viewModel::refreshData)
        binding.errorView.actionOneClickListener = {
            logDebug("errorView reloadClickListener", TAG)
            viewModel.refreshData()
        }
        binding.emptyStateView.reloadClickListener = {
            logDebug("emptyStateView reloadClickListener", TAG)
            viewModel.refreshData()
        }
        binding.customToolbar.settingsClickListener = {
            logDebug("customToolbar settingsClickListener", TAG)
            showSettingsMenu()
        }
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }

        setFragmentResultListener(PROFILE_VERIFICATION_WAIT_REQUEST_KEY) { _, bundle ->
            val profileVerificationType =
                bundle.getParcelableCompat<ProfileVerificationType>(
                    PROFILE_VERIFICATION_TYPE_KEY
                )
            profileVerificationType?.let { type ->
                when (type) {
                    is ProfileVerificationType.ProfileVerificationReady -> {
                        preferences.saveAppStatus(AppStatus.REGISTERED)
                        openPendingDocument()
                    }

                    is ProfileVerificationType.ProfileVerificationError -> viewModel.showErrorMessage(
                        errorMessageRes = type.errorMessageRes ?: return@let
                    )

                    is ProfileVerificationType.ProfileVerificationRejected -> viewModel.showErrorMessage(
                        errorMessageRes = R.string.sdk_error_user_profile_rejected
                    )
                }
            }
        }
    }

    override fun subscribeToLiveData() {
        viewModel.adapterListLiveData.observe(viewLifecycleOwner) {
            setAdapterData(it)
        }
        viewModel.openEditUserLiveDataEvent.observe(viewLifecycleOwner) {
            if (viewModel.isUserProfileIdentified.not()) {
                evrotrustSDKHelper.openEditProfile(activity = requireActivity())
            }
        }
        viewModel.openDocumentLiveDataEvent.observe(viewLifecycleOwner) {
            openPendingDocument()
        }
        evrotrustSDKHelper.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                showMessage(Message.error(it))
            }
            viewModel.refreshData()
        }
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) { status ->
            viewModel.onSdkStatusChanged(status)
        }
    }

    private fun setAdapterData(data: PagedList<PendingAdapterMarker>) {
        logDebug("setAdapterData size: ${data.size}", TAG)
        binding.refreshLayout.isRefreshing = false
        adapter.submitList(data)
        setupEmptyState(size = data.size)
    }

    private fun setupEmptyState(size: Int?) {
        logDebug("setupEmptyState size: $size", TAG)
        updateEmptyStateJob?.cancel()
        updateEmptyStateJob = lifecycleScope.launch {
            if (size == null || size == 0) {
                delay(DELAY_500)
                showEmptyState()
                viewModel.setHasNewUnsignedDocuments(value = false)
            } else {
                hideEmptyState()
            }
        }
    }

    override fun onDocumentClicked(document: PendingDocumentUi) {
        logDebug(
            "onDocumentClicked evrotrustTransactionId: ${document.evrotrustTransactionId}",
            TAG
        )
        viewModel.setPendingDocument(document = document)
        evrotrustSDKHelper.checkUserStatus()
    }

    override fun onResume() {
        super.onResume()
        adapter.registerChangeStateObserver(
            observer = adapterDataObserver,
            changeStateListener = {
                logDebug("adapterDataObserver stateChanged", TAG)
                setupEmptyState(adapter.currentList?.size)
            }
        )
        viewModel.refreshData()
    }


    override fun onPause() {
        super.onPause()
        logDebug("onPause", TAG)
        adapter.unregisterChangeStateObserver(adapterDataObserver)
    }

    override fun onAlertDialogResult(result: AlertDialogResult) {
        if (result.isPositive) {
            logDebug("onAlertDialogResult isPositive", TAG)
            evrotrustSDKHelper.openEditProfile(requireActivity())
        }
    }

    private fun openPendingDocument() {
        viewModel.pendingDocument?.let { document ->
            evrotrustSDKHelper.openDocument(
                activity = requireActivity(),
                evrotrustTransactionId = document.evrotrustTransactionId
            )
        }
    }
}