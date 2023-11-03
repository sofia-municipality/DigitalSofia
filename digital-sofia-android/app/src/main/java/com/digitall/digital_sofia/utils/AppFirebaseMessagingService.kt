package com.digitall.digital_sofia.utils

import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class AppFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    companion object {
        private const val TAG = "AppFirebaseMessagingServiceTag"
    }

    private val preferences: PreferencesRepository by inject()

    override fun onNewToken(token: String) {
        if (token.isEmpty()) {
            logError("onNewToken token.isNullOrEmpty()", TAG)
            return
        }
        logDebug("onNewToken token: $token", TAG)
        preferences.saveFirebaseToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        logDebug("onMessageReceived", TAG)
    }
}