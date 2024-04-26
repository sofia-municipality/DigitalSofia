package com.digital.sofia.domain.repository.network.common

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.firebase.FirebaseTokenRequestModel
import com.digital.sofia.domain.models.user.LogLevelModel
import kotlinx.coroutines.flow.Flow
import java.io.File

interface CommonNetworkRepository {

    fun updateFirebaseToken(
        data: FirebaseTokenRequestModel
    ) : Flow<ResultEmittedData<Unit>>

    fun getLogLevel(
        personalIdentifier: String,
    ): Flow<ResultEmittedData<LogLevelModel>>

}