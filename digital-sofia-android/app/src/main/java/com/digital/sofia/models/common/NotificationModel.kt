package com.digital.sofia.models.common

sealed class NotificationModel {
    abstract val isValid: Boolean

    data class DocumentStatusNotificationModel(val status: String?) : NotificationModel() {
        override val isValid: Boolean
            get() = status.isNullOrEmpty().not()
    }

    data class AuthorizationNotificationModel(val code: String?, val expiresAt: String?) :
        NotificationModel() {
        override val isValid: Boolean
            get() = code.isNullOrEmpty().not() && expiresAt.isNullOrEmpty().not()
    }

    data class UserProfileStatusChangeNotificationModel(
        val isIdentified: Boolean?,
        val isReadyToSign: Boolean?,
        val isRejected: Boolean?,
        val isSupervised: Boolean?
    ) : NotificationModel() {
        override val isValid: Boolean
            get() = isIdentified != null && isReadyToSign != null && isRejected != null && isSupervised != null
    }

}