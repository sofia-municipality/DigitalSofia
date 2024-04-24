/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.common

import java.io.Serializable

data class PinCode(
    val errorCount: Int,
    val hashedPin: String?,
    val errorTimeCode: Long?,
    val encryptedPin: String?,
    val decryptedPin: String?,
    val errorStatus: ErrorStatus,
    val biometricStatus: BiometricStatus,
) : Serializable {

    fun validate(): Boolean {
        return !decryptedPin.isNullOrEmpty() &&
                !hashedPin.isNullOrEmpty()
    }

    fun validateWithEncrypted(): Boolean {
        return !encryptedPin.isNullOrEmpty() &&
                !decryptedPin.isNullOrEmpty() &&
                !hashedPin.isNullOrEmpty()
    }

}