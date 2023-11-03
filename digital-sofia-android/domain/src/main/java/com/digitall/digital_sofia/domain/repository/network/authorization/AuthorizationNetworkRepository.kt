package com.digitall.digital_sofia.domain.repository.network.authorization

import com.digitall.digital_sofia.domain.models.authorization.AuthorizationModel
import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import kotlinx.coroutines.flow.Flow

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface AuthorizationNetworkRepository {

    fun enterToAccount(
        hashedPin: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<AuthorizationModel>>

    fun getAccessToken(
        refreshToken: String,
    ): Flow<ResultEmittedData<AuthorizationModel>>

}