/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.pin.create

import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentChangePinCreateBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.fragments.base.pin.create.BaseCreatePinFragment
import com.digital.sofia.ui.view.CodeKeyboardView
import com.digital.sofia.ui.view.CodeView
import com.digital.sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePinCreateFragment :
    BaseCreatePinFragment<FragmentChangePinCreateBinding, ChangePinCreateViewModel>() {

    companion object {
        private const val TAG = "ChangePinCreateFragmentTag"
    }

    override fun getViewBinding() = FragmentChangePinCreateBinding.inflate(layoutInflater)

    override val viewModel: ChangePinCreateViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override fun setupControls() {
        super.setupControls()
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
    }

    override fun subscribeToLiveData() {
        super.subscribeToLiveData()
        evrotrustSDKHelper.errorMessageResLiveData.observe(viewLifecycleOwner) {
            if (it != null && it != 0) {
                logDebug("errorMessageResLiveData showBannerMessage", TAG)
                showMessage(Message.error(it))
            }
        }
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
            logDebug("sdkStatusLiveData onSdkStatusChanged status: ${it.name}", TAG)
            viewModel.onSdkStatusChanged(it)
        }
    }

    override fun getPasscodeView(): CodeView {
        return binding.passcodeView
    }

    override fun getPassCodeKeyboardView(): CodeKeyboardView {
        return binding.keyboard
    }

    override fun setEnterPassCodeState() {
        binding.tvDescription.setText(R.string.change_pin_create_description_1)
    }

    override fun setConfirmPassCodeState() {
        binding.tvDescription.setText(R.string.change_pin_create_description_2)
    }

}