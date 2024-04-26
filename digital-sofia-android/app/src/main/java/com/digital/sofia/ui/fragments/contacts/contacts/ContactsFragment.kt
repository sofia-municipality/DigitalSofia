/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.contacts.contacts

import com.digital.sofia.R
import com.digital.sofia.data.BuildConfig.URL_BASE_WEB_VIEW
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.ui.fragments.base.BaseWebViewFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsFragment : BaseWebViewFragment<ContactsViewModel>() {

    companion object {
        private const val TAG = "ContactsFragmentTag"
    }

    override val showToolbar: Boolean = true

    override val showBeta: Boolean = false

    override val showSettingsButton: Boolean = false

    override val toolbarNavigationIconRes: Int = R.drawable.ic_back

    override val toolbarNavigationTextRes: Int = R.string.back

    override val needRestoreState: Boolean = false

    override val shouldPersistView: Boolean = false

    override val shouldHandleBackClickHandler: Boolean = true

    override val viewModel: ContactsViewModel by viewModel()

    private val preferences: PreferencesRepository by inject()

    override fun onCreated() {
        try {
            val token = preferences.readAccessToken()?.token
            val refreshToken = preferences.readRefreshToken()?.token
            val language = preferences.readCurrentLanguage().type
            val url = if (!token.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                URL_BASE_WEB_VIEW + "contacts/?hideNav=true&token=$token&refreshToken=$refreshToken&lang=$language"
            } else {
                URL_BASE_WEB_VIEW + "contacts/?hideNav=true&lang=$language"
            }
            logDebug("loadWebPage url: $url", TAG)
            loadWebPage(url, shouldClearHistory = true)
        } catch (e: IllegalStateException) {
            logError("loadWebPage Exception: ${e.message}", e, TAG)
        }
    }

    override fun refreshScreen() {
        super.refreshScreen()
        onCreated()
    }

}