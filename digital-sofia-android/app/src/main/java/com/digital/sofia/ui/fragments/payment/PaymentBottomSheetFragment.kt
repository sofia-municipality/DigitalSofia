package com.digital.sofia.ui.fragments.payment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import com.digital.sofia.databinding.FragmentBaseWebViewBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.ui.fragments.base.BaseBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentBottomSheetFragment(private val listener: Listener) :
    BaseBottomSheetFragment<FragmentBaseWebViewBinding, PaymentBottomSheetViewModel>() {

    companion object {
        private const val TAG = "PaymentBottomSheetFragmentTag"
        const val PAYMENT_URL_KEY = "PAYMENT_URL_KEY"

        fun newInstance(url: String?, listener: Listener) =
            PaymentBottomSheetFragment(listener).apply {
                arguments = bundleOf(PAYMENT_URL_KEY to url)
            }
    }

    override val viewModel: PaymentBottomSheetViewModel by viewModel()

    override fun getViewBinding() = FragmentBaseWebViewBinding.inflate(layoutInflater)

    override val maxHeight = Resources.getSystem().displayMetrics.heightPixels * 90 / 100

    private var currentScrollPositionY = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun initViews() {
        binding.webView.visibility = View.GONE
        binding.webView.webViewClient = PaymentWebViewClient()
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.setInitialScale(1)
        binding.webView.isVerticalScrollBarEnabled = false
        binding.customToolbar.visibility = View.GONE
        binding.webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            currentScrollPositionY = scrollY
        }
        parseArguments()
    }


    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        listener.operationCompleted()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.visibility = View.VISIBLE
        binding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.visibility = View.GONE
        binding.webView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            binding.webView.removeAllViews()
            binding.webView.destroy()
        } catch (e: Exception) {
            /* do nothing */
            logError(
                "onDestroyView eException: ${e.message}", e,
                TAG
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.operationCompleted()
    }

    override fun onStateChange(state: Int) {
        if (state == BottomSheetBehavior.STATE_DRAGGING && currentScrollPositionY > 0) {
            setExpandedState()
        }
    }

    private fun parseArguments() {
        try {
            val paymentUrl = arguments?.getString(PAYMENT_URL_KEY)
            paymentUrl?.let {
                binding.webView.loadUrl(it)
            }
        } catch (exception: IllegalStateException) {
            logError("parseArguments Exception: ${exception.message}", exception, TAG)
        }
    }

    private inner class PaymentWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            logDebug("shouldOverrideUrlLoading", TAG)
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            logDebug("onPageStarted", TAG)
        }

        override fun onPageFinished(view: WebView, url: String) {
            logDebug("onPageFinished", TAG)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?,
        ) {
            super.onReceivedError(view, request, error)
            logError(
                "onReceivedError error code: ${error?.errorCode} description: ${error?.description}",
                TAG
            )
        }
    }

    interface Listener {
        fun operationCompleted()
    }
}