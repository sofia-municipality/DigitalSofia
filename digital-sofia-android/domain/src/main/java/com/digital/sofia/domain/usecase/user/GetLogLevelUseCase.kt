package com.digital.sofia.domain.usecase.user

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.user.LogLevelModel
import com.digital.sofia.domain.repository.network.common.CommonNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow


class GetLogLevelUseCase(
    private val commonNetworkRepository: CommonNetworkRepository
) {

    companion object {
        private const val TAG = "GetLogLevelUseCase"
    }

    fun invoke(
        personalIdentifier: String,
    ): Flow<ResultEmittedData<LogLevelModel>> {
        logDebug("getLogLevel", TAG)
        return commonNetworkRepository.getLogLevel(
            personalIdentifier = personalIdentifier,
        )
    }

}