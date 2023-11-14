package com.digitall.digital_sofia.domain.models.authorization

import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class AuthorizationModel(
    val scope: String,
    val expiresIn: String,
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
    val sessionState: String,
    val notBeforePolicy: String,
    val refreshExpiresIn: String,
) : Serializable
