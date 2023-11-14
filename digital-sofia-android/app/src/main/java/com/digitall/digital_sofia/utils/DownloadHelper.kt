package com.digitall.digital_sofia.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.readOnly

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DownloadHelper(
    private val downloadManager: DownloadManager,
    private val preferences: PreferencesRepository,
) {

    companion object {
        private const val TAG = "DownloadHelperTag"
    }

    private var downloadId: Long = 0L

    private val _onReadyLiveData = SingleLiveEvent<Unit>()
    val onReadyLiveData = _onReadyLiveData.readOnly()

    private val _onErrorLiveData = SingleLiveEvent<Unit>()
    val onErrorLiveData = _onErrorLiveData.readOnly()

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            onReceive(context)
        }
    }

    @SuppressLint("Range")
    private fun onReceive(context: Context?) {
        if (downloadId != 0L) {
            val cursor =
                downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            if (cursor.moveToFirst()) {
                when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        logDebug("downloadFile status STATUS_SUCCESSFUL", TAG)
                        _onReadyLiveData.call()
                    }

                    DownloadManager.STATUS_FAILED -> {
                        logError("downloadFile status STATUS_FAILED", TAG)
                        _onErrorLiveData.call()
                    }

                    DownloadManager.STATUS_PENDING -> {
                        logError("downloadFile status STATUS_PENDING", TAG)
                        _onErrorLiveData.call()
                    }

                    DownloadManager.STATUS_PAUSED -> {
                        logError("downloadFile status STATUS_PAUSED", TAG)
                        _onErrorLiveData.call()
                    }
                }
            }
        }
        context?.unregisterReceiver(downloadReceiver)
        downloadId = 0L
    }

    fun downloadFile(
        context: Context,
        url: String,
    ) {
        logDebug("downloadFile url: $url", TAG)
        try {
            val token = preferences.readAccessToken()
            val title = "document_${System.currentTimeMillis()}.pdf"
            val request = DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(title)
                .setDescription(title)
                .addRequestHeader("Authorization", "Bearer $token")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
            downloadId = downloadManager.enqueue(request)
            if (downloadId != 0L) {
                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                context.registerReceiver(downloadReceiver, filter)
            } else {
                _onErrorLiveData.call()
            }
        } catch (e: Exception) {
            logError("downloadFile Exception: ${e.message}", e, TAG)
            _onErrorLiveData.call()
        }
    }
}

