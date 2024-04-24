/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.settings.pin.enter

import com.digital.sofia.databinding.FragmentChangePinEnterBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.ui.fragments.base.pin.enter.BaseEnterPinFragment
import com.digital.sofia.ui.view.CodeKeyboardView
import com.digital.sofia.ui.view.CodeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePinEnterFragment :
    BaseEnterPinFragment<FragmentChangePinEnterBinding, ChangePinEnterViewModel>() {

    companion object {
        private const val TAG = "ChangePinEnterFragmentTag"
    }

    override fun getViewBinding() = FragmentChangePinEnterBinding.inflate(layoutInflater)

    override val viewModel: ChangePinEnterViewModel by viewModel()

    override fun getPasscodeView(): CodeView {
        return binding.passcodeView
    }

    override fun getKeyboardView(): CodeKeyboardView {
        return binding.keyboard
    }

    override val countOfDigits: Int = 6

    override fun setupControls() {
        super.setupControls()
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
    }

}