package com.digital.sofia.domain.usecase.firebase

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.firebase.FirebaseTokenRequestModel
import com.digital.sofia.domain.repository.network.common.CommonNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

class UpdateFirebaseTokenUseCase(
    private val commonNetworkRepository: CommonNetworkRepository
) {

    companion object {
        private const val TAG = "UpdateFirebaseTokenUseCase"
    }

    fun invoke(token: String): Flow<ResultEmittedData<Unit>> {
        logDebug("updateFirebaseToken", TAG)
        return commonNetworkRepository.updateFirebaseToken(
            data = FirebaseTokenRequestModel(token = token)
        )
    }

}