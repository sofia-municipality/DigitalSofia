package com.digitall.digital_sofia.domain.models.common

import com.digitall.digital_sofia.domain.models.base.TypeEnum

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

enum class AppStatus (override val type: String) : TypeEnum {
    NOT_READY("NOT_READY"),
    NOT_SIGNED_DOCUMENT("NOT_SIGNED_DOCUMENT"),
    NOT_SEND_SIGNED_DOCUMENT("NOT_SEND_SIGNED_DOCUMENT"),
    READY("READY"),
}