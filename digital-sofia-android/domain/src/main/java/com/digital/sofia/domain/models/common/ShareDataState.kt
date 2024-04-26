package com.digital.sofia.domain.models.common

import com.digital.sofia.domain.models.base.TypeEnum

enum class ShareDataState(override val type: String): TypeEnum {
    REQUEST_IDENTITY("REQUEST_IDENTITY"),
    IDENTITY_REQUESTED("IDENTITY_REQUESTED"),
    IDENTITY_SIGNED("IDENTITY_SIGNED"),
    IDENTITY_AUTHENTICATED("IDENTITY_AUTHENTICATED"),
}