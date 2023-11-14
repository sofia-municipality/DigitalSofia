package com.digitall.digital_sofia.data.mappers.network.authorization

import com.digitall.digital_sofia.data.mappers.base.BaseMapper
import com.digitall.digital_sofia.data.models.network.authorization.AuthorizationResponse
import com.digitall.digital_sofia.domain.models.authorization.AuthorizationModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class AuthorizationResponseMapper : BaseMapper<AuthorizationResponse, AuthorizationModel>() {

    override fun map(from: AuthorizationResponse): AuthorizationModel {
        return with(from) {
            AuthorizationModel(
                scope = scope,
                expiresIn = expiresIn,
                tokenType = tokenType,
                accessToken = accessToken,
                refreshToken = refreshToken,
                sessionState = sessionState,
                notBeforePolicy = notBeforePolicy,
                refreshExpiresIn = refreshExpiresIn,
            )
        }
    }

}