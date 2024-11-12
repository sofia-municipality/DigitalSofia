/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.models.documents.DocumentDownloadModel

class DownloadHelper(
    private val downloadManager: DownloadManager,
    private val preferences: PreferencesRepository,
) {

    companion object {
        private const val TAG = "DownloadHelperTag"
        private const val DOWNLOAD_FOLDER_NAME = "DigitalSofia"
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
                        _onReadyLiveData.callOnMainThread()
                    }

                    DownloadManager.STATUS_FAILED -> {
                        logError("downloadFile status STATUS_FAILED", TAG)
                        _onErrorLiveData.callOnMainThread()
                    }

                    DownloadManager.STATUS_PENDING -> {
                        logError("downloadFile status STATUS_PENDING", TAG)
                        _onErrorLiveData.callOnMainThread()
                    }

                    DownloadManager.STATUS_PAUSED -> {
                        logError("downloadFile status STATUS_PAUSED", TAG)
                        _onErrorLiveData.callOnMainThread()
                    }
                }
            }
        }
        context?.unregisterReceiver(downloadReceiver)
        downloadId = 0L
    }

    fun downloadFile(
        context: Context,
        downloadModel: DocumentDownloadModel,
    ) {
        logDebug("downloadFile url: ${downloadModel.url}", TAG)
        try {
            val token = preferences.readAccessToken()?.token
            val request = DownloadManager.Request(Uri.parse(downloadModel.url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(downloadModel.name)
                .setDescription(downloadModel.name)
                .addRequestHeader("Authorization", "Bearer $token")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$DOWNLOAD_FOLDER_NAME/${downloadModel.name}")
            downloadId = downloadManager.enqueue(request)
            if (downloadId != 0L) {
                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(
                        downloadReceiver,
                        filter,
                        Context.RECEIVER_EXPORTED
                    )
                } else {
                    context.registerReceiver(
                        downloadReceiver,
                        filter
                    )
                }
            } else {
                _onErrorLiveData.callOnMainThread()
            }
        } catch (e: Exception) {
            logError("downloadFile Exception: ${e.message}", e, TAG)
            _onErrorLiveData.callOnMainThread()
        }
    }
}

