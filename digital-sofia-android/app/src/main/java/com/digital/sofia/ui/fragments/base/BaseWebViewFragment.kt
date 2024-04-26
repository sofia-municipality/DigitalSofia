/**
 * domStorageEnabled for http
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.ui.fragments.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebBackForwardList
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import com.digital.sofia.R
import com.digital.sofia.databinding.FragmentBaseWebViewBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.JavaScriptDownloadFileInterface
import java.lang.ref.SoftReference

abstract class BaseWebViewFragment<VM : BaseViewModel> :
    BaseFragment<FragmentBaseWebViewBinding, VM>() {

    companion object {
        private const val FILE_CHOOSER_RESULT_CODE = 12345
        private const val TAG = "BaseWebViewFragmentTag"
        private const val JS_INTERFACE_NAME = "download_files_android"
    }

    override fun getViewBinding() = FragmentBaseWebViewBinding.inflate(layoutInflater)

    protected abstract val showToolbar: Boolean

    protected abstract val showSettingsButton: Boolean

    protected abstract val toolbarNavigationIconRes: Int?

    protected abstract val toolbarNavigationTextRes: Int?

    protected abstract val showBeta: Boolean

    protected abstract val needRestoreState: Boolean

    protected abstract val shouldHandleBackClickHandler: Boolean

    open val textAppearance: Int = R.style.TextStyle_20spRegularMainColor

    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    private var savedInstanceState: Bundle? = null

    protected abstract val shouldPersistView: Boolean

    private var persistedView = SoftReference<View?>(null)

    private var shouldClearHistory = false

    private lateinit var downloadBroadcastReceiver: BroadcastReceiver

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (needRestoreState) {
            saveWebViewState(outState)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val persistedView = persistedView.get()
        val view = if (persistedView == null) {
            super.onCreateView(inflater, container, savedInstanceState)
        } else persistedView

        return if (shouldPersistView) {
            persistingView(view)
        } else view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            this.savedInstanceState = savedInstanceState
        }

        super.onViewCreated(view, this.savedInstanceState)
    }

    @CallSuper
    @SuppressLint("SetJavaScriptEnabled")
    override fun setupView() {
        logDebug("setupView", TAG)
        binding.webView.visibility = View.GONE
        binding.webView.webViewClient = CustomWebViewClient()
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
        binding.webView.addJavascriptInterface(
            JavaScriptDownloadFileInterface(
                context = requireContext(),
                onDownloadCompleted = {
                    showDownloadSuccessMessage()
                },
            ),
            JS_INTERFACE_NAME
        )
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                logDebug("onShowFileChooser", TAG)
                uploadMessage?.onReceiveValue(null)
                uploadMessage = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE)
                return true
            }
        }
        binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            if (url.startsWith("blob:")) {
                val script = JavaScriptDownloadFileInterface.fetchBlobScript(
                    blobUrl = url,
                    contentDisposition = contentDisposition,
                    mimetype = mimetype,
                )
                binding.webView.evaluateJavascript(script, null)
                return@setDownloadListener
            }
            performFileDownload(
                url = url,
                mimetype = mimetype,
                userAgent = userAgent,
                onDownloadCompleted = {
                    showDownloadSuccessMessage()
                })
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
                        if (shouldHandleBackClickHandler) {
                            onBackPressed()
                        }
                    }
                )
            }
            toolbarNavigationTextRes?.let {
                binding.customToolbar.showNavigationText(it, textAppearance)
            }
        } else {
            binding.customToolbar.visibility = View.GONE
        }
        binding.btnBeta.visibility = if (showBeta) View.VISIBLE else View.GONE
        binding.btnBeta.onClickThrottle {
            viewModel.showBetaState()
        }
        subscribeToNewTokensEvents()
    }

    private fun subscribeToNewTokensEvents() {
        viewModel.newTokensEventLiveData.observe(viewLifecycleOwner) {
            refreshScreen()
        }
    }

    protected fun restoreWebViewState(bundle: Bundle): WebBackForwardList? {
        return if (shouldPersistView && persistedView.get() != null) {
            binding.webView.copyBackForwardList()
        } else binding.webView.restoreState(bundle)
    }

    protected fun stopWebViewLoading() {
        binding.webView.stopLoading()
    }

    private fun saveWebViewState(bundle: Bundle) {
        try {
            binding.webView.saveState(bundle)
        } catch (ignored: Throwable) {
            if (savedInstanceState != null) {
                bundle.putAll(savedInstanceState)
            }
        }
    }

    private fun persistingView(view: View?): View? {
        val root = persistedView.get()
        return if (root == null) {
            persistedView = SoftReference(view)
            view
        } else {
            (root.parent as? ViewGroup)?.removeView(root)
            root
        }
    }

    private fun performFileDownload(
        url: String,
        mimetype: String,
        userAgent: String,
        onDownloadCompleted: () -> Unit
    ) {
        val uri = Uri.parse(url)
        val paths = uri.path?.split("/")
        var fileName = paths?.last() ?: ""
        if (!fileName.contains(".")) {
            fileName += ".${MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)}"
        }

        val intentFilter = IntentFilter("android.intent.action.DOWNLOAD_COMPLETE")
        downloadBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                showDownloadSuccessMessage()
            }
        }

        val request = DownloadManager.Request(uri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setTitle(fileName)
            setDescription(fileName)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            addRequestHeader("User-Agent", userAgent)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }
        val downloadManager = requireContext().getSystemService(DownloadManager::class.java)
        ContextCompat.registerReceiver(
            requireContext(),
            downloadBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        downloadManager.enqueue(request)
    }

    private fun showDownloadSuccessMessage() {
        requireActivity().runOnUiThread {
            showMessage(
                Message(
                    title = StringSource.Res(R.string.information),
                    message = StringSource.Res(R.string.file_download_success),
                    type = Message.Type.ALERT,
                    positiveButtonText = StringSource.Res(R.string.ok)
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            logDebug("onActivityResult FILE_CHOOSER_RESULT_CODE", TAG)
            if (uploadMessage == null) return
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            uploadMessage?.onReceiveValue(result?.let { arrayOf(it) })
            uploadMessage = null
        }
    }

    @CallSuper
    override fun setupControls() {
    }

    open fun needToLoadPage(url: String?): Boolean = true

    protected fun loadWebPage(
        pageUrl: String,
        headers: Map<String, String>? = null,
        shouldClearHistory: Boolean = false
    ) {
        logDebug("loadWebPage pageUrl: $pageUrl", TAG)
        this.shouldClearHistory = shouldClearHistory
        if (headers.isNullOrEmpty()) {
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
        binding.webView.visibility = View.VISIBLE
        binding.webView.onResume()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        if (needRestoreState) {
            savedInstanceState = Bundle().also { bundle ->
                saveWebViewState(bundle)
            }
        }
        binding.webView.visibility = View.GONE
        binding.webView.onPause()
    }

    @CallSuper
    override fun onDestroyView() {
        if (!shouldPersistView) {
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
        }

        override fun onPageFinished(view: WebView, url: String) {
            logDebug("onPageFinished", TAG)
            if (shouldClearHistory) {
                binding.webView.clearHistory()
            }
            shouldClearHistory = false
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
}