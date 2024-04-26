package com.digital.sofia.utils

import com.digital.sofia.domain.models.firebase.FirebaseTokenModel
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingServiceHelper(
    private val preferences: PreferencesRepository,
) {

    companion object {
        private const val TAG = "FirebaseMessagingServiceHelperTag"
    }

    private val _newFirebaseMessageLiveData = SingleLiveEvent<RemoteMessage>()
    val newFirebaseMessageLiveData = _newFirebaseMessageLiveData.readOnly()

    private val _newTokenEventLiveData = SingleLiveEvent<Unit>()
    val newTokenEventLiveData = _newTokenEventLiveData.readOnly()

    fun onNewToken(token: String) {
        if (token.isEmpty()) {
            logError("onNewToken token.isNullOrEmpty()", TAG)
            return
        }
        logDebug("onNewToken token: $token", TAG)
        val tokenModel =  FirebaseTokenModel(isSend = false, token = token)
        preferences.saveFirebaseToken(
            value = tokenModel
        )
        _newTokenEventLiveData.callOnMainThread()
    }

    fun onMessageReceived(message: RemoteMessage) {
        logDebug(
            "onMessageReceived title: ${message.notification?.title} body:${message.notification?.body}",
            TAG
        )
        if (message.notification == null) {
            logError("onMessageReceived message.notification == null", TAG)
            return
        }
        if (message.notification?.title.isNullOrEmpty()) {
            logError("onMessageReceived message.notification?.title.isNullOrEmpty()", TAG)
            return
        }
        if (message.notification?.body.isNullOrEmpty()) {
            logError("onMessageReceived message.notification?.body.isNullOrEmpty()", TAG)
            return
        }
        if (message.data.isEmpty()) {
            logError("onMessageReceived message.data.isEmpty()", TAG)
        }
        _newFirebaseMessageLiveData.setValueOnMainThread(message)
    }
}