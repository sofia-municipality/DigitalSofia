package com.digitall.digital_sofia.ui.fragments.settings.pin.create

import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.FragmentChangePinCreateBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.fragments.base.pin.create.BaseCreateCodeFragment
import com.digitall.digital_sofia.ui.view.CodeKeyboardView
import com.digitall.digital_sofia.ui.view.CodeView
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ChangePinCreateFragment :
    BaseCreateCodeFragment<FragmentChangePinCreateBinding, ChangePinCreateViewModel>() {

    companion object {
        private const val TAG = "ChangePinCreateFragmentTag"
    }

    override fun getViewBinding() = FragmentChangePinCreateBinding.inflate(layoutInflater)

    override val viewModel: ChangePinCreateViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override val countOfDigits: Int = 6

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
                showBannerMessage(BannerMessage.error(it))
            }
        }
        evrotrustSDKHelper.sdkStatusLiveData.observe(viewLifecycleOwner) {
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