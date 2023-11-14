package com.digitall.digital_sofia.utils

import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Additional class to help to setup activities. It combines some common logic
 * between different activities. Also could take responsibility for some logic
 * delegation of activity tasks.
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

class ActivitiesCommonHelper(
    private val preferences: PreferencesRepository,
) {

    companion object {
        private const val TAG = "ActivitiesCommonHelperTag"
    }

    /**
     * We will fetch firebase token on first start and then track it through
     * FirebaseMessagingService
     */
    fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                logError("getFcmToken registration failed: ${task.exception}", TAG)
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            if (token.isNullOrEmpty()) {
                logError("getFcmToken token.isNullOrEmpty()", TAG)
                return@addOnCompleteListener
            }
            logDebug("getFcmToken token: $token", TAG)
            preferences.saveFirebaseToken(token)
        }
    }

    fun applyLightDarkTheme() {
//        when (preferencesRepository.readAppThemeType()) {
//            AppThemeType.FOLLOW_SYSTEM -> {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//            }
//            AppThemeType.LIGHT -> {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            }
//            AppThemeType.DARK -> {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            }
//        }
    }
}