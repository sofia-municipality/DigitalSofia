/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.network.authorization

import com.digital.sofia.domain.models.authorization.AuthorizationModel
import com.digital.sofia.domain.models.base.ResultEmittedData
import kotlinx.coroutines.flow.Flow

interface AuthorizationNetworkRepository {

    fun enterToAccount(
        hashedPin: String,
        firebaseToken: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<AuthorizationModel>>

    fun refreshAccessToken(
        refreshToken: String,
    ): Flow<ResultEmittedData<AuthorizationModel>>

}