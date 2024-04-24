/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.authorization

import java.io.Serializable

data class AuthorizationModel(
    val scope: String?,
    val expiresIn: Long?,
    val tokenType: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val sessionState: String?,
    val refreshExpiresIn: Long?,
    val notBeforePolicy: String?,
) : Serializable
