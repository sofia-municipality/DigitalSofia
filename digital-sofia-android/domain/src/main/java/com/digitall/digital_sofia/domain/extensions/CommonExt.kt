package com.digitall.digital_sofia.domain.extensions

import com.digitall.digital_sofia.domain.models.base.TypeEnum
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

inline fun <reified T : Enum<T>> getEnumTypeValue(type: String): T? {
    val values = enumValues<T>()
    return values.firstOrNull {
        it is TypeEnum && (it as TypeEnum).type.equals(type, true)
    }
}

@Throws(NoSuchAlgorithmException::class)
fun String.sha256(): String? {
    return try {
        val bytes = this.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val result = StringBuilder()
        for (byte in digest) {
            result.append(String.format("%02x", byte))
        }
        result.toString()
    } catch (e: Exception) {
        logError("sha256() Exception: ${e.message}", e, "sha256Tag")
        null
    }
}

fun String.capitalized(): String {
    return this.substring(0, 1).uppercase() + this.substring(1).lowercase();
}