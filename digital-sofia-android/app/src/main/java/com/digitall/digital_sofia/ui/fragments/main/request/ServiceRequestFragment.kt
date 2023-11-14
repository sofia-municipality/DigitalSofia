package com.digitall.digital_sofia.ui.fragments.main.request

import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.data.URL_BASE_WEB_VIEW
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.ui.fragments.base.BaseWebViewFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ServiceRequestFragment :
    BaseWebViewFragment<ServiceRequestViewModel>() {

    companion object {
        private const val TAG = "ServiceRequestFragmentTag"
    }

    override val showToolbar: Boolean = true

    override val showSettingsButton: Boolean = true

    override val toolbarNavigationIconRes: Int = R.drawable.img_logo_small

    override val toolbarNavigationTextRes: Int? = null

    override val viewModel: ServiceRequestViewModel by viewModel()

    private val preferences: PreferencesRepository by inject()

    override fun onCreated() {
        try {
            val accessToken = preferences.readAccessToken()
            val refreshToken = preferences.readRefreshToken()
            val url = if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                URL_BASE_WEB_VIEW + "request-service/?hideNav=true&token=$accessToken&refreshToken=$refreshToken"
            } else {
                logError("onCreated accessToken or refreshToken isNullOrEmpty", TAG)
                URL_BASE_WEB_VIEW + "request-service/?hideNav=true"
            }
            logDebug("loadWebPage url: $url", TAG)
            loadWebPage(url)
        } catch (e: IllegalStateException) {
            logError("loadWebPage Exception: ${e.message}", e, TAG)
        }

    }

}