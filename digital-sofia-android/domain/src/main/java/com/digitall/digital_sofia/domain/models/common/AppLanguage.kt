package com.digitall.digital_sofia.domain.models.common

import com.digitall.digital_sofia.domain.models.base.TypeEnum

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

enum class AppLanguage(override val type: String, val nameString: String): TypeEnum {
    EN("en", "English"),
    BG("bg", "Български"),
}