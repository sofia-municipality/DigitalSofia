/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.common

import com.digital.sofia.domain.models.base.TypeEnum

enum class AppLanguage(override val type: String, val nameString: String): TypeEnum {
    EN("en", "English"),
    BG("bg", "Български"),
}