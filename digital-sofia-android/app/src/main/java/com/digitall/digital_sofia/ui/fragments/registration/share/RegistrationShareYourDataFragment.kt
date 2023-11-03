package com.digitall.digital_sofia.ui.fragments.registration.share

import com.digitall.digital_sofia.databinding.FragmentRegistrationShareYourDataBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationShareYourDataFragment :
    BaseFragment<FragmentRegistrationShareYourDataBinding, RegistrationShareYourDataViewModel>() {

    companion object {
        private const val TAG = "RegistrationShareYourDataFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationShareYourDataBinding.inflate(layoutInflater)

    override val viewModel: RegistrationShareYourDataViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override fun onResume() {
        super.onResume()
        viewModel.updateDocuments()
    }

    override fun setupControls() {
        binding.btnYes.onClickThrottle {
            logDebug("btnYes onClickThrottle", TAG)
            viewModel.proceedNext()
        }
        binding.btnNo.onClickThrottle {
            logDebug("btnNo onClickThrottle", TAG)
            viewModel.onNoClicked()
        }
        binding.errorView.reloadClickListener = {
            logDebug("reloadClickListener", TAG)
            viewModel.proceedNext()
        }
    }

    private fun openDocument(evrotrustTransactionId: String) {
        logDebug("openDocument evrotrustTransactionId: $evrotrustTransactionId", TAG)
        if (evrotrustTransactionId.isNotEmpty()) {
            evrotrustSDKHelper.openDocument(
                activity = requireActivity(),
                evrotrustTransactionId = evrotrustTransactionId
            )
        }
    }

    override fun subscribeToLiveData() {
        viewModel.openDocumentViewLiveData.observe(viewLifecycleOwner) {
            openDocument(it)
        }
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            viewModel.onSdkStatusChanged(it)
        }
        evrotrustSDKHelper.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                showBannerMessage(BannerMessage.error(it))
            }
        }
    }

}