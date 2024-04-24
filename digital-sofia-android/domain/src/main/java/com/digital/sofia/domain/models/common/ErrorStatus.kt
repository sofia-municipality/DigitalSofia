/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.common

import com.digital.sofia.domain.models.base.TypeEnum

enum class ErrorStatus(val timeoutMillis: Long, override val type: String) : TypeEnum {
    NO_TIMEOUT(0L, "NO_TIMEOUT"),
    TIMEOUT_30_SECONDS(30_000L, "TIMEOUT_30_SECONDS"),
    TIMEOUT_5_MINUTES(300_000L, "TIMEOUT_5_MINUTES"),
    TIMEOUT_1_HOUR(3_600_000L, "TIMEOUT_1_HOUR"),
    TIMEOUT_24_HOUR(86_400_000L, "TIMEOUT_24_HOUR"),
}