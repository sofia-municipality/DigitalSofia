package com.digital.sofia.domain.models.token

import java.io.Serializable

data class RefreshTokenModel(
    val token: String,
    val expirationTime: Long
): Serializable {}