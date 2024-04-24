package com.digital.sofia.data.repository.network.logs

import com.digital.sofia.data.mappers.network.logs.request.UploadFilesRequestMapper
import com.digital.sofia.data.network.logs.LogsApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.repository.network.logs.LogsNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class LogsNetworkRepositoryImpl(
    private val logsApi: LogsApi,
    private val uploadFilesRequestMapper: UploadFilesRequestMapper,
    private val authorizationHelper: AuthorizationHelper,
    coroutineContextProvider: CoroutineContextProvider,
) : LogsNetworkRepository,
    BaseRepository(
        coroutineContextProvider = coroutineContextProvider,
    ) {

    companion object {
        private const val TAG = "LogsNetworkRepository"
    }

    override suspend fun refreshAccessToken() {
        authorizationHelper.startAuthorization()
    }

    override fun uploadLogs(
        personalIdentifier: String,
        files: List<File>
    ) = flow {
        logDebug("uploadLogs", TAG)
        getResult {
            logsApi.uploadLogs(
                personalIdentifier = personalIdentifier,
                files = uploadFilesRequestMapper.map(files)
            )
        }.onLoading {
            logDebug("uploadLogs onLoading", TAG)
        }.onSuccess {
            logDebug("uploadLogs onSuccess", TAG)
            emit(ResultEmittedData.success(Unit))
        }.onRetry {
            logDebug("uploadLogs onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("uploadLogs onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)
}