/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.signing

import androidx.core.widget.NestedScrollView
import com.digital.sofia.databinding.FragmentHomeBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.enableChangeAnimations
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.forms.HomeAdapterMarker
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.ui.fragments.main.documents.DocumentsFragment
import com.digital.sofia.ui.fragments.main.signing.list.SigningAdapter
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SigningFragment :
    BaseFragment<FragmentHomeBinding, SigningViewModel>(),
    SigningAdapter.ClickListener {

    companion object {
        private const val TAG = "HomeFragmentTag"
    }

    override val viewModel: SigningViewModel by viewModel()

    private val adapter: SigningAdapter by inject()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

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
        binding.nestedScrollView.setOnScrollChangeListener { nestedScrollView: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (nestedScrollView != null) {
                val lastChild = nestedScrollView.getChildAt(nestedScrollView.childCount - 1)
                if (lastChild != null) {
                    if ((scrollY >= (lastChild.measuredHeight - nestedScrollView.measuredHeight))
                        && scrollY > oldScrollY
                        && !viewModel.isLastPage()
                        && !viewModel.isLoading()
                    ) {
                        viewModel.loadMoreDocuments()
                    }
                }
            }
        }
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
    }

    override fun subscribeToLiveData() {
        viewModel.adapterList.observe(viewLifecycleOwner) {
            setAdapterData(it)
        }
        viewModel.openEditUserLiveDataEvent.observe(viewLifecycleOwner) {
            if (viewModel.isUserProfileIdentified.not()) {
                evrotrustSDKHelper.openEditProfile(activity = requireActivity())
            }
        }
        viewModel.openDocumentLiveDataEvent.observe(viewLifecycleOwner) {
            viewModel.evrotrustTransactionId?.let { transactionId ->
                evrotrustSDKHelper.openDocument(
                    activity = requireActivity(),
                    evrotrustTransactionId = transactionId
                )
            }
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

    private fun setAdapterData(data: List<HomeAdapterMarker>) {
        logDebug("setAdapterData size: ${data.size}", TAG)
        binding.refreshLayout.isRefreshing = false
        adapter.items = data
    }

    override fun onDocumentClicked(evrotrustTransactionId: String) {
        logDebug("onDocumentClicked evrotrustTransactionId: $evrotrustTransactionId", TAG)
        viewModel.evrotrustTransactionId = evrotrustTransactionId
        evrotrustSDKHelper.checkUserStatus()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}