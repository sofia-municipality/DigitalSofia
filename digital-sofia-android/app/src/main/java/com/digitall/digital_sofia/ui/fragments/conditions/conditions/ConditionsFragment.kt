package com.digitall.digital_sofia.ui.fragments.conditions.conditions

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

class ConditionsFragment : BaseWebViewFragment<ConditionsViewModel>() {

    companion object {
        private const val TAG = "ConditionsFragmentTag"
    }

    override val viewModel: ConditionsViewModel by viewModel()

    override val showToolbar: Boolean = true

    override val showSettingsButton: Boolean = false

    override val toolbarNavigationIconRes: Int = R.drawable.ic_back

    override val toolbarNavigationTextRes: Int = R.string.back

    private val preferences: PreferencesRepository by inject()

    override fun onCreated() {
        try {
            val token = preferences.readAccessToken()
            val refreshToken = preferences.readRefreshToken()
            val url = if (!token.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                URL_BASE_WEB_VIEW + "terms-and-conditions/?hideNav=true&token=$token&refreshToken=$refreshToken"
            } else {
                URL_BASE_WEB_VIEW + "terms-and-conditions/?hideNav=true"
            }
            logDebug("loadWebPage url: $url", TAG)
            loadWebPage(url)
        } catch (e: IllegalStateException) {
            logError("loadWebPage Exception: ${e.message}", e, TAG)
        }
    }

}