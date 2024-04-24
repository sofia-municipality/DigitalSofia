/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.error.biometric

import com.digital.sofia.databinding.BottomSheetBiometricErrorBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.ui.fragments.base.BaseBottomSheetFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class BiometricErrorBottomSheetFragment :
    BaseBottomSheetFragment<BottomSheetBiometricErrorBinding, BiometricErrorBottomSheetViewModel>() {

    companion object {
        private const val TAG = "PermissionBottomSheetFragmentTag"
    }

    override fun getViewBinding() = BottomSheetBiometricErrorBinding.inflate(layoutInflater)

    override val viewModel: BiometricErrorBottomSheetViewModel by viewModel()

    override fun initViews() {
        setupControls()
    }

    private fun setupControls() {
        binding.btnClose.onClickThrottle {
            logDebug("btnNext onClickThrottle", TAG)
            dismiss()
        }
    }

}