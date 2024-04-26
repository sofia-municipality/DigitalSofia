/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.mappers.network.authorization

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.authorization.AuthorizationResponse
import com.digital.sofia.domain.models.authorization.AuthorizationModel

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