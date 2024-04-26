/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.share

import com.digital.sofia.databinding.FragmentRegistrationShareYourDataBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.fragments.base.BaseFragment
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPinShareYourDataFragment :
    BaseFragment<FragmentRegistrationShareYourDataBinding, ForgotPinShareYourDataViewModel>() {

    companion object {
        private const val TAG = "RegistrationShareYourDataFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationShareYourDataBinding.inflate(layoutInflater)

    override val viewModel: ForgotPinShareYourDataViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override fun setupControls() {
        binding.btnYes.onClickThrottle {
            logDebug("btnYes onClickThrottle", TAG)
            viewModel.proceedNext()
        }
        binding.btnNo.onClickThrottle {
            logDebug("btnNo onClickThrottle", TAG)
            viewModel.toConfirmIdentificationFragment()
        }
        binding.errorView.actionOneClickListener = {
            logDebug("actionOneClickListener", TAG)
            viewModel.proceedNext()
        }
        binding.errorView.actionTwoClickListener = {
            logDebug("actionTwoClickListener", TAG)
            viewModel.toConfirmIdentificationFragment()
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
                showMessage(Message.error(it))
            }
        }
    }

}