package com.digitall.digital_sofia.ui.fragments.main.documents.preview

import androidx.navigation.fragment.navArgs
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.ui.fragments.base.BaseWebViewFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DocumentPreviewFragment :
    BaseWebViewFragment<DocumentPreviewViewModel>() {

    companion object {
        private const val TAG = "DocumentPreviewFragmentTag"
    }

    override val showToolbar: Boolean = true

    override val showSettingsButton: Boolean = true

    override val toolbarNavigationIconRes: Int = R.drawable.img_logo_small

    override val toolbarNavigationTextRes: Int? = null

    override val viewModel: DocumentPreviewViewModel by viewModel()

    private val preferences: PreferencesRepository by inject()

    private val args: DocumentPreviewFragmentArgs by navArgs()

    override fun onCreated() {
        try {
            val url = args.url
//            val accessToken = preferences.readAccessToken()
//            val refreshToken = preferences.readRefreshToken()
//            val url = if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
//                BASE_WEB_VIEW_URL + "request-service/?hideNav=true&token=$accessToken&refreshToken=$refreshToken"
//            } else {
//                logError("onCreated accessToken or refreshToken isNullOrEmpty", TAG)
//                BASE_WEB_VIEW_URL + "request-service/?hideNav=true"
//            }
            logDebug("loadWebPage url: $url", TAG)
            loadWebPage(url)
        } catch (e: IllegalStateException) {
            logError("loadWebPage Exception: ${e.message}", e, TAG)
        }

    }

}