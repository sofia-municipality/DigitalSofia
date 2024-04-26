package com.digital.sofia.domain.usecase.logs

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.repository.network.logs.LogsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow
import java.io.File

class UploadLogsUseCase(
    private val logsNetworkRepository: LogsNetworkRepository,
) {

    companion object {
        private const val TAG = "UploadLogsUseCase"
    }

    fun invoke(
        personalIdentifier: String,
        files: List<File>,
    ): Flow<ResultEmittedData<Unit>> {
        logDebug("uploadLogs", TAG)
        return logsNetworkRepository.uploadLogs(
            personalIdentifier = personalIdentifier,
            files = files,
        )
    }

}