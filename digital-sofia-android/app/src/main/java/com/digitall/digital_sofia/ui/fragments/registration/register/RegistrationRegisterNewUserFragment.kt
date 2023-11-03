package com.digitall.digital_sofia.ui.fragments.registration.register

import com.digitall.digital_sofia.databinding.FragmentRegistrationSignDocumentBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationRegisterNewUserFragment :
    BaseFragment<FragmentRegistrationSignDocumentBinding, RegistrationRegisterNewUserViewModel>() {

    companion object {
        private const val TAG = "RegistrationRegisterNewUserFragmentTag"
    }

    override fun getViewBinding() = FragmentRegistrationSignDocumentBinding.inflate(layoutInflater)

    override val viewModel: RegistrationRegisterNewUserViewModel by viewModel()

    override fun onCreated() {
        viewModel.registerNewUser()
    }

    override fun setupControls() {
        binding.errorView.reloadClickListener = {
            logDebug("reloadClickListener", TAG)
            viewModel.registerNewUser()
        }
    }
}