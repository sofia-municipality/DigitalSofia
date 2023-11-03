package com.digitall.digital_sofia.utils

import android.Manifest
import android.app.Activity
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import java.io.File

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ScreenshotsDetectorImpl(
    private val context: CurrentContext,
    private val handler: Handler
) : ScreenshotsDetector {

    private var lastHandledUri: String? = null
    private var screenshotListener: ((File?) -> Unit)? = null

    private val contentObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            getScreenshotFile(uri)?.let {
                screenshotListener?.invoke(it)
            }
        }
    }

    override fun startDetecting(activity: Activity, listener: (File?) -> Unit) {
        screenshotListener = listener
        activity.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    override fun stopDetecting(activity: Activity) {
        activity.contentResolver.unregisterContentObserver(contentObserver)
        screenshotListener = null
    }

    private fun getScreenshotFile(uri: Uri?): File? {
        if (!isReadStoragePermissionGranted() || lastHandledUri == uri?.toString()) return null

        return uri?.let {
            val file = File(UriUtils.getRealFilePathFromUri(context.get(), uri).orEmpty())
            if (isScreenshotPath(file.absolutePath)) {
                lastHandledUri = it.toString()
                file
            } else null
        }
    }

    private fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionsManager.isPermissionGranted(
                context.get(),
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            PermissionsManager.isPermissionGranted(
                context.get(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun isScreenshotPath(path: String?): Boolean {
        val lowercasePath = path?.lowercase()
        val screenshotDirectory = getPublicScreenshotDirectoryName()?.lowercase()
        return lowercasePath?.contains("/.") == false &&
                ((screenshotDirectory != null && lowercasePath.contains(screenshotDirectory)) ||
                        lowercasePath.contains("screenshot"))
    }

    private fun getPublicScreenshotDirectoryName(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).name
        } else null
    }
}