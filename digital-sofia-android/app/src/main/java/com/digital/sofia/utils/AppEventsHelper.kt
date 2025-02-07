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
import com.digital.sofia.models.common.NotificationModel
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
        private const val IS_IDENTIFIED_KEY = "isIdentified"
        private const val IS_READY_TO_SIGN_KEY = "isReadyToSign"
        private const val IS_REJECTED_KEY = "isRejected"
        private const val IS_SUPERVISED_KEY = "isSupervised"
    }

    private val _newAppEventLiveData = MutableLiveData<NotificationModel?>()
    val newAppEventLiveData = _newAppEventLiveData.readOnly()

    private val _documentsForSignLiveData = LiveEvent<Boolean>()
    val documentsForSignLiveData = _documentsForSignLiveData.readOnly()


    var hasNewAuthorizationEvent = false
    var hasNewPendingDocumentEvent = false
    var hasNewSignedDocumentEvent = false
    var hasNewUserProfileStatusChangeEvent = false

    var isNotificationEvent = false
        private set

    fun setHasNewUnsignedDocuments(value: Boolean) {
        _documentsForSignLiveData.setValueOnMainThread(value)
    }

    fun onNewIntent(intent: Intent?) {
        logDebug("onNewIntent", TAG)
        val bundle = intent?.extras ?: return
        isNotificationEvent = true
        val notification = when {
            bundle.getString(STATUS_KEY).isNullOrEmpty()
                .not() -> NotificationModel.DocumentStatusNotificationModel(
                status = bundle.getString(
                    STATUS_KEY
                )
            )

            bundle.getString(CODE_KEY).isNullOrEmpty().not() && bundle.getString(EXPIRES_AT_KEY)
                .isNullOrEmpty().not() -> NotificationModel.AuthorizationNotificationModel(
                code = bundle.getString(CODE_KEY),
                expiresAt = bundle.getString(EXPIRES_AT_KEY),
            )

            bundle.getString(IS_IDENTIFIED_KEY).isNullOrEmpty().not() && bundle.getString(
                IS_READY_TO_SIGN_KEY
            ).isNullOrEmpty().not() && bundle.getString(IS_REJECTED_KEY).isNullOrEmpty()
                .not() && bundle.getString(IS_SUPERVISED_KEY).isNullOrEmpty()
                .not() -> NotificationModel.UserProfileStatusChangeNotificationModel(
                isIdentified = bundle.getString(IS_IDENTIFIED_KEY).toBoolean(),
                isSupervised = bundle.getString(IS_SUPERVISED_KEY).toBoolean(),
                isRejected = bundle.getString(IS_REJECTED_KEY).toBoolean(),
                isReadyToSign = bundle.getString(IS_READY_TO_SIGN_KEY).toBoolean(),
            )

            else -> null
        }

        handleNotification(notificationModel = notification)
        _newAppEventLiveData.setValueOnMainThread(notification)
    }

    fun onNewFirebaseMessage(message: RemoteMessage) {
        logDebug("onNewFirebaseMessage", TAG)
        isNotificationEvent = false
        if (message.data.isEmpty()) {
            logError("onNewFirebaseMessage message.data.isEmpty()", TAG)
            notificationHelper.showNotificationOnMainThread(
                title = StringSource.Text(message.notification?.title ?: "N/A"),
                content = StringSource.Text(message.notification?.body ?: "N/A"),
            )
            return
        }
        val notification = when {
            message.data[STATUS_KEY].isNullOrEmpty()
                .not() -> NotificationModel.DocumentStatusNotificationModel(
                status = message.data[STATUS_KEY]
            )

            message.data[CODE_KEY].isNullOrEmpty().not() && message.data[EXPIRES_AT_KEY]
                .isNullOrEmpty().not() -> NotificationModel.AuthorizationNotificationModel(
                code = message.data[CODE_KEY],
                expiresAt = message.data[EXPIRES_AT_KEY],
            )

            message.data[IS_IDENTIFIED_KEY].isNullOrEmpty().not() && message.data[
                IS_READY_TO_SIGN_KEY
            ].isNullOrEmpty().not() && message.data[IS_REJECTED_KEY].isNullOrEmpty()
                .not() && message.data[IS_SUPERVISED_KEY].isNullOrEmpty()
                .not() -> NotificationModel.UserProfileStatusChangeNotificationModel(
                isIdentified = message.data[IS_IDENTIFIED_KEY].toBoolean(),
                isSupervised = message.data[IS_SUPERVISED_KEY].toBoolean(),
                isRejected = message.data[IS_REJECTED_KEY].toBoolean(),
                isReadyToSign = message.data[IS_READY_TO_SIGN_KEY].toBoolean(),
            )

            else -> null
        }

        handleNotification(notificationModel = notification)
        _newAppEventLiveData.setValueOnMainThread(notification)
        val bundle = Bundle().apply {
            message.data.forEach { (key, value) ->
                putString(key, value)
            }
        }
        notificationHelper.showNotificationOnMainThread(
            bundle = bundle,
            title = StringSource.Text(message.notification?.title ?: "N/A"),
            content = StringSource.Text(message.notification?.body ?: "N/A"),
        )
    }

    private fun handleNotification(
        notificationModel: NotificationModel?,
    ) {
        if (notificationModel?.isValid == true) {
            when (notificationModel) {
                is NotificationModel.AuthorizationNotificationModel -> hasNewAuthorizationEvent =
                    true

                is NotificationModel.DocumentStatusNotificationModel -> notificationModel.status?.let { status ->
                    getEnumTypeValue<DocumentStatusModel>(status.lowercase())?.let {
                        when (it) {
                            DocumentStatusModel.PENDING,
                            DocumentStatusModel.DELIVERING,
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

                is NotificationModel.UserProfileStatusChangeNotificationModel -> hasNewUserProfileStatusChangeEvent =
                    true
            }
        }
    }
}