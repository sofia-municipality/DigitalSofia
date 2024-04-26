package com.digital.sofia.domain.models.token

import java.io.Serializable

data class AccessTokenModel(
    val token: String,
    val expirationTime: Long
): Serializable {}
