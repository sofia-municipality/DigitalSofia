package com.digital.sofia.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.digital.sofia.R
import com.digital.sofia.domain.utils.LogUtil.logError
import java.io.File

class JavaScriptDownloadFileInterface(
    private val context: Context,
    private val onDownloadCompleted: () -> Unit
) {

    private var filename: String? = null

    companion object {

        private const val JS_INTERFACE_NAME = "download_files_android"
        private const val NOTIFICATION_CHANNEL_ID = "123"
        private const val DOWNLOAD_NOTIFICATION_ID = 123456
        private const val DOWNLOAD_FOLDER_NAME = "DigitalSofia"
        private const val TAG = "JavaScriptDownloadFileInterfaceTag"

        fun fetchBlobScript(
            blobUrl: String,
            contentDisposition: String,
            mimetype: String,

        ): String {
            return """
                (async () => {
                  const response = await fetch('${blobUrl}', {
                    headers: {
                      'Content-Type': '${mimetype}',
                    }
                  });
                  const blob = await response.blob();
                  const reader = new FileReader();
                  reader.addEventListener('load', () => {
                    const base64 = reader.result.replace(/^data:.+;base64,/, '');
                    ${JS_INTERFACE_NAME}.receiveBase64(
                      base64,
                      '${blobUrl}',
                      '${contentDisposition}',
                      '${mimetype}'
                    );
                  });
                  reader.readAsDataURL(blob); 
                })();
            """.trimIndent()
        }
    }

    @JavascriptInterface
    fun sendFilename(filename: String) {
        this.filename = filename
    }

    @JavascriptInterface
    fun receiveBase64(
        base64: String,
        url: String,
        contentDisposition: String,
        mimetype: String,
    ) {
        try {
            val rootPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
            val rootDirectory = File(rootPath)
            if (!rootDirectory.exists()) {
                val created = rootDirectory.mkdirs()
                if (!created) {
                    logError("Root download directory not created", TAG)
                    return
                }
            }
            val downloadsDirectory = File(rootPath, DOWNLOAD_FOLDER_NAME)
            if (!downloadsDirectory.exists()) {
                val created = downloadsDirectory.mkdirs()
                if (!created) {
                    logError("Downloads directory not created", TAG)
                    return
                }
            }
            val content = Base64.decode(base64, Base64.DEFAULT)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)
            var filename = this.filename ?: URLUtil.guessFileName(url, contentDisposition, mimetype)
            if (!filename.contains(".")) {
                filename += ".$extension"
            }
            var file = File(
                downloadsDirectory,
                filename
            )
            if (file.exists()) {
                val filesCount = downloadsDirectory.listFiles()
                    ?.count { it.name.startsWith(file.nameWithoutExtension) } ?: 0
                file = File(
                    downloadsDirectory,
                    "${file.nameWithoutExtension}-${filesCount}.$extension"
                )
            }
            file.writeBytes(content)
            this.filename = null

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.provider", file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    fileUri, mimetype
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.img_logo_big)
                .setColor(ContextCompat.getColor(context, R.color.color_white_text))
                .setContentTitle(filename)
                .setContentText("File downloaded")
                .setSound(sound)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            onDownloadCompleted()

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            NotificationManagerCompat.from(context)
                .notify(DOWNLOAD_NOTIFICATION_ID, builder.build())
        } catch (exception: Exception) {
            logError("receiveBase64 Exception: ${exception.message}", exception, TAG)
        }
    }

}