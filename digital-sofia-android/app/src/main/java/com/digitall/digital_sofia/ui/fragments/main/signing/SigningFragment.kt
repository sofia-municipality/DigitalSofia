package com.digitall.digital_sofia.ui.fragments.main.signing

import androidx.lifecycle.lifecycleScope
import com.digitall.digital_sofia.databinding.FragmentHomeBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.enableChangeAnimations
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.forms.HomeAdapterMarker
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.ui.fragments.main.signing.list.SigningAdapter
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

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

    override fun setupView() {
        binding.rvDocuments.adapter = adapter
        adapter.clickListener = this
        binding.rvDocuments.enableChangeAnimations(false)
        binding.rvDocuments.isNestedScrollingEnabled = false
    }

    override fun setupControls() {
        binding.refreshLayout.setOnRefreshListener(viewModel::refreshScreen)
        binding.errorView.reloadClickListener = {
            logDebug("errorView reloadClickListener", TAG)
            viewModel.refreshScreen()
        }
        binding.emptyStateView.reloadClickListener = {
            logDebug("emptyStateView reloadClickListener", TAG)
            viewModel.refreshScreen()
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
        viewModel.adapterList.onEach {
            setAdapterData(it)
        }.launch(lifecycleScope)
        evrotrustSDKHelper.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                showBannerMessage(BannerMessage.error(it))
            }
            viewModel.refreshScreen()
        }
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            viewModel.onSdkStatusChanged(it)
        }
    }

    private fun setAdapterData(data: List<HomeAdapterMarker>) {
        logDebug("setAdapterData size: ${data.size}", TAG)
        binding.refreshLayout.isRefreshing = false
        adapter.items = data
    }

    override fun onDocumentClicked(evrotrustTransactionId: String) {
        logDebug("onDocumentClicked evrotrustTransactionId: $evrotrustTransactionId", TAG)
        evrotrustSDKHelper.openDocument(
            activity = requireActivity(),
            evrotrustTransactionId = evrotrustTransactionId
        )
    }

}