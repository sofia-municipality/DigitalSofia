package com.digitall.digital_sofia.domain.models.common

import com.digitall.digital_sofia.domain.models.base.TypeEnum

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

enum class AppThemeType(override val type: String): TypeEnum {
    FOLLOW_SYSTEM("follow_system"),
    DARK("dark"),
    LIGHT("light")
}