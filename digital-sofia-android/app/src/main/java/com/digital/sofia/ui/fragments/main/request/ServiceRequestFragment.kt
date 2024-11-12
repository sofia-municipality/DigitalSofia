/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.request

import android.net.Uri
import android.os.Bundle
import com.digital.sofia.R
import com.digital.sofia.data.BuildConfig.PAYMENT_HOST
import com.digital.sofia.data.BuildConfig.URL_BASE_WEB_VIEW
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.ui.fragments.base.BaseWebViewFragment
import com.digital.sofia.ui.fragments.payment.PaymentBottomSheetFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ServiceRequestFragment :
    BaseWebViewFragment<ServiceRequestViewModel>(),
    PaymentBottomSheetFragment.Listener {

    companion object {
        private const val TAG = "ServiceRequestFragmentTag"
    }

    override val showToolbar: Boolean = true

    override val showBeta: Boolean = true

    override val showSettingsButton: Boolean = true

    override val toolbarNavigationIconRes: Int = R.drawable.img_logo_small

    override val toolbarNavigationTextRes: Int? = null

    override val needRestoreState: Boolean = true

    override val shouldPersistView: Boolean = true

    override val shouldKeepBinding: Boolean = true

    override val shouldHandleBackClickHandler: Boolean = false

    override val viewModel: ServiceRequestViewModel by viewModel()

    private val preferences: PreferencesRepository by inject()

    private var paymentBottomSheet: PaymentBottomSheetFragment? = null

    override fun onCreated(savedInstanceState: Bundle?) {
        try {
            val accessToken = preferences.readAccessToken()?.token
            val refreshToken = preferences.readRefreshToken()?.token
            val language = preferences.readCurrentLanguage().type
            val url = if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                URL_BASE_WEB_VIEW + "request-service/?hideNav=true&token=$accessToken&refreshToken=$refreshToken&lang=$language&showRequestServiceLink=true"
            } else {
                logError("onCreated accessToken or refreshToken isNullOrEmpty", TAG)
                URL_BASE_WEB_VIEW + "request-service/?hideNav=true&lang=$language&showRequestServiceLink=true"
            }

            if (needRestoreState) {
                if (savedInstanceState != null) {
                    val history = restoreWebViewState(savedInstanceState)
                    if (history != null) {
                        for (index in 0..<history.size) {
                            val initialPage = history.getItemAtIndex(index)
                            val initialPageUri = Uri.parse(initialPage.url)
                            val pageLanguage = initialPageUri.getQueryParameter("lang")
                            val pageRefreshToken = initialPageUri.getQueryParameter("refreshToken")
                            val isLanguageChanged = pageLanguage != null && pageLanguage != language
                            val isRefreshTokenChanged =
                                pageRefreshToken != null && pageRefreshToken != refreshToken
                            if (isLanguageChanged || isRefreshTokenChanged) {
                                stopWebViewLoading()
                                loadWebPage(pageUrl = url, shouldClearHistory = true)
                                break
                            }
                        }
                    } else {
                        loadWebPage(url)
                    }
                } else {
                    loadWebPage(url)
                }
            } else {
                logDebug("loadWebPage url: $url", TAG)
                loadWebPage(url)
            }
        } catch (e: IllegalStateException) {
            logError("loadWebPage Exception: ${e.message}", e, TAG)
        }
    }

    override fun refreshScreen() {
        super.refreshScreen()
        try {
            val accessToken = preferences.readAccessToken()?.token
            val refreshToken = preferences.readRefreshToken()?.token
            val language = preferences.readCurrentLanguage().type
            val url = if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                URL_BASE_WEB_VIEW + "request-service/?hideNav=true&token=$accessToken&refreshToken=$refreshToken&lang=$language&showRequestServiceLink=true"
            } else {
                logError("onCreated accessToken or refreshToken isNullOrEmpty", TAG)
                URL_BASE_WEB_VIEW + "request-service/?hideNav=true&lang=$language&showRequestServiceLink=true"
            }
            logDebug("loadWebPage url: $url", TAG)
            loadWebPage(url, shouldClearHistory = true)
        } catch (e: IllegalStateException) {
            logError("loadWebPage Exception: ${e.message}", e, TAG)
        }
    }

    override fun needToLoadPage(url: String?): Boolean {
        val uri = Uri.parse(url)
        return when {
            uri.host == PAYMENT_HOST -> {
                if (paymentBottomSheet == null) {
                    paymentBottomSheet =
                        PaymentBottomSheetFragment.newInstance(url = url, listener = this)
                            .also { bottomSheet ->
                                bottomSheet.show(
                                    requireActivity().supportFragmentManager,
                                    "PaymentBottomSheetFragmentTag"
                                )
                            }
                }
                false
            }

            else -> true
        }
    }

    override fun operationCompleted() {
        paymentBottomSheet?.dismiss().also {
            paymentBottomSheet = null
            refreshScreen()
        }
    }
}