package com.digitall.digital_sofia.ui.activity.main

import android.content.Intent
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.ui.activity.base.BaseActivity
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Use single activity
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

class MainActivity : BaseActivity<MainViewModel>() {

    companion object {
        private const val TAG = "MainActivityTag"
    }

    override val viewModel: MainViewModel by viewModel()

    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logDebug("onActivityResult requestCode: $requestCode resultCode: $resultCode", TAG)
        evrotrustSDKHelper.onActivityResult(
            requestCode = requestCode,
            data = data,
        )
    }

}