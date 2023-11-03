package com.digitall.digital_sofia.ui.fragments.base

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import com.digitall.digital_sofia.databinding.FragmentBaseWebViewBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.ui.BaseViewModel

/**
 * domStorageEnabled for http
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

abstract class BaseWebViewFragment<VM : BaseViewModel> :
    BaseFragment<FragmentBaseWebViewBinding, VM>() {

    companion object {
        private const val TAG = "BaseWebViewFragmentTag"
    }

    override fun getViewBinding() = FragmentBaseWebViewBinding.inflate(layoutInflater)

    protected abstract val showToolbar: Boolean

    protected abstract val showSettingsButton: Boolean

    protected abstract val toolbarNavigationIconRes: Int?

    protected abstract val toolbarNavigationTextRes: Int?

    @CallSuper
    @SuppressLint("SetJavaScriptEnabled")
    override fun setupView() {
        logDebug("setupView", TAG)
        binding.webView.apply {
            webViewClient = CustomWebViewClient()
            webChromeClient = WebChromeClient()
            settings.apply {
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                javaScriptEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                domStorageEnabled = true
            }
            setInitialScale(1)
            isVerticalScrollBarEnabled = false
        }
        if (showToolbar) {
            binding.customToolbar.visibility = View.VISIBLE
            if (showSettingsButton) {
                binding.customToolbar.showSettingsIcon(
                    settingsClickListener = {
                        showSettingsMenu()
                    }
                )
            }
            toolbarNavigationIconRes?.let {
                binding.customToolbar.showNavigationIcon(
                    iconRes = it,
                    navigationClickListener = {
                        onBackPressed()
                    }
                )
            }
            toolbarNavigationTextRes?.let {
                binding.customToolbar.showNavigationText(it)
            }
        } else {
            binding.customToolbar.visibility = View.GONE
        }
    }

    @CallSuper
    override fun setupControls() {
        binding.refreshLayout.setOnRefreshListener(binding.webView::reload)
    }

    open fun needToLoadPage(url: String?): Boolean = true

    protected fun loadWebPage(pageUrl: String, headers: Map<String, String>? = null) {
        logDebug("loadWebPage pageUrl: $pageUrl", TAG)
        if(headers.isNullOrEmpty()) {
            binding.webView.loadUrl(pageUrl)
        } else {
            binding.webView.loadUrl(pageUrl, headers)
        }
    }

    @CallSuper
    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        if (binding.webView.canGoBack()) binding.webView.goBack()
        else viewModel.onBackPressed()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    @CallSuper
    override fun onDestroyView() {
        try {
            binding.webView.removeAllViews()
            binding.webView.destroy()
        } catch (e: Exception) {
            /* do nothing */
        }
        super.onDestroyView()
    }

    inner class CustomWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            logDebug("shouldOverrideUrlLoading", TAG)
            return !needToLoadPage(request?.url.toString())
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            logDebug("onPageStarted", TAG)
            binding.refreshLayout.isRefreshing = false
        }

        override fun onPageFinished(view: WebView, url: String) {
            logDebug("onPageFinished", TAG)
            binding.refreshLayout.isRefreshing = false
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?,
        ) {
            super.onReceivedError(view, request, error)
            logError("onReceivedError", TAG)
        }
    }
}