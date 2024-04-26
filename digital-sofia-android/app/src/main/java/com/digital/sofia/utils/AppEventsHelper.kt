package com.digital.sofia.utils

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.digital.sofia.domain.extensions.getEnumTypeValue
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.StringSource
import com.google.firebase.messaging.RemoteMessage

class AppEventsHelper(
    private val notificationHelper: NotificationHelper,
) {

    companion object {
        private const val TAG = "AppEventsHelperTag"
        private const val CODE_KEY = "code"
        private const val EXPIRES_AT_KEY = "expiresAt"
        private const val STATUS_KEY = "status"
    }

    private val _newAppEventLiveData = MutableLiveData<Unit>()
    val newAppEventLiveData = _newAppEventLiveData.readOnly()

    private val _documentsForSignLiveData = LiveEvent<Boolean>()
    val documentsForSignLiveData = _documentsForSignLiveData.readOnly()


    var hasNewAuthorizationEvent = false
    var hasNewPendingDocumentEvent = false
    var hasNewSignedDocumentEvent = false

    var isNotificationEvent = false
        private set

    fun setHasNewUnsignedDocuments(value: Boolean) {
        _documentsForSignLiveData.setValueOnMainThread(value)
    }

    fun onNewIntent(intent: Intent?) {
        logDebug("onNewIntent", TAG)
        val bundle = intent?.extras ?: return
        isNotificationEvent = true
        handleData(
            code = bundle.getString(CODE_KEY),
            expires = bundle.getString(EXPIRES_AT_KEY),
            status = bundle.getString(STATUS_KEY),
        )
        _newAppEventLiveData.setValueOnMainThread(null)
    }

    fun onNewFirebaseMessage(message: RemoteMessage) {
        logDebug("onNewFirebaseMessage", TAG)
        isNotificationEvent = false
        if (message.data.isEmpty()) {
            logError("onNewFirebaseMessage message.data.isEmpty()", TAG)
            notificationHelper.showNotificationOnMainThread(
                title = StringSource.Text(message.notification?.title!!),
                content = StringSource.Text(message.notification?.body!!),
            )
            return
        }
        handleData(
            code = message.data[CODE_KEY],
            expires = message.data[EXPIRES_AT_KEY],
            status = message.data[STATUS_KEY],
        )
        _newAppEventLiveData.setValueOnMainThread(null)
        val bundle = Bundle().apply {
            message.data.forEach { (key, value) ->
                putString(key, value)
            }
        }
        notificationHelper.showNotificationOnMainThread(
            bundle = bundle,
            title = StringSource.Text(message.notification?.title!!),
            content = StringSource.Text(message.notification?.body!!),
        )
    }

    private fun handleData(
        code: String?,
        expires: String?,
        status: String?,
    ) {

        if (!code.isNullOrEmpty() && !expires.isNullOrEmpty()) {
            hasNewAuthorizationEvent = true
        }
        if (!status.isNullOrEmpty()) {
            getEnumTypeValue<DocumentStatusModel>(status)?.let {
                when (it) {
                    DocumentStatusModel.PENDING,
                    DocumentStatusModel.SIGNING -> {
                        hasNewPendingDocumentEvent = true
                    }

                    DocumentStatusModel.EXPIRED,
                    DocumentStatusModel.SIGNED -> {
                        hasNewSignedDocumentEvent = true
                    }

                    else -> {
                        // not need
                    }
                }
            }

        }
    }
}