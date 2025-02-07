/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.network.documents

import com.digital.sofia.data.mappers.network.documents.request.DocumentAuthenticationRequestBodyMapper
import com.digital.sofia.data.mappers.network.documents.response.DocumentResponseMapper
import com.digital.sofia.data.mappers.network.documents.response.DocumentStatusResponseMapper
import com.digital.sofia.data.mappers.network.documents.response.DocumentsResponseMapper
import com.digital.sofia.data.network.documents.DocumentsApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.DownloadProgress
import com.digital.sofia.domain.models.documents.DocumentAuthenticationRequestModel
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.domain.models.documents.DocumentsWithPaginationModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class DocumentsNetworkRepositoryImpl(
    private val documentsApi: DocumentsApi,
    private val documentResponseMapper: DocumentResponseMapper,
    private val documentsResponseMapper: DocumentsResponseMapper,
    private val documentStatusResponseMapper: DocumentStatusResponseMapper,
    private val documentAuthenticationRequestBodyMapper: DocumentAuthenticationRequestBodyMapper,
    private val authorizationHelper: AuthorizationHelper,
    coroutineContextProvider: CoroutineContextProvider,
) : DocumentsNetworkRepository, BaseRepository(
    coroutineContextProvider = coroutineContextProvider,
) {

    companion object {
        private const val TAG = "DocumentsNetworkRepositoryTag"
    }

    override suspend fun refreshAccessToken() {
        authorizationHelper.startAuthorization()
    }

    override fun getDocuments(
        cursor: String?,
        status: String?,
    ): Flow<ResultEmittedData<DocumentsWithPaginationModel>> = flow {
        logDebug("getDocuments", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            documentsApi.getDocument(
                cursor = cursor,
                status = status,
            )
        }.onSuccess {
            logDebug("getDocuments onSuccess", TAG)
            val result = documentsResponseMapper.map(it)
            emit(ResultEmittedData.success(result))
        }.onRetry {
            logDebug("getDocuments onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("getDocuments onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

    override fun downloadFile(
        file: File,
        documentFormIOId: String,
    ): Flow<ResultEmittedData<DownloadProgress>> = flow {
        logDebug("downloadFile documentFormIOId: $documentFormIOId", TAG)
        emit(ResultEmittedData.loading())
        try {
            val response = documentsApi.downloadFile(
                documentFormIOId = documentFormIOId,
            )
            if (!response.isSuccessful) {
                emit(
                    ResultEmittedData.error(
                        ResultEmittedData.Error(
                            serverMessage = null,
                            responseCode = response.code(),
                            serverType = response.headers()["Server"],
                            responseMessage = response.message()
                        )
                    )
                )
                return@flow
            }
            val body = response.body() ?: throw IllegalStateException("Response body is null")
            val fileSize = body.contentLength()
            val inputStream = body.byteStream()
            file.outputStream().use { outputStream ->
                logDebug("downloadFile outputStream", TAG)
                val dataBuffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead: Long = 0
                while (inputStream.read(dataBuffer).also { bytesRead = it } != -1) {
                    logDebug("downloadFile read", TAG)
                    outputStream.write(dataBuffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = ((totalBytesRead * 100) / fileSize).toInt()
                    emit(ResultEmittedData.success(DownloadProgress.Loading(progress.toString())))
                }
            }
            logDebug("downloadFile success", TAG)
            emit(ResultEmittedData.success(DownloadProgress.Ready))
        } catch (e: Exception) {
            logError("downloadFile Exception: ${e.message}", e, TAG)
            emit(
                ResultEmittedData.error(
                    ResultEmittedData.Error(
                        responseCode = 0,
                        serverType = null,
                        serverMessage = null,
                        responseMessage = e.message
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    override fun checkSignedDocumentStatus(
        evrotrustTransactionId: String,
    ): Flow<ResultEmittedData<DocumentStatusModel>> = flow {
        logDebug("checkSignedDocumentStatus", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            documentsApi.checkSignedDocumentStatus(
                evrotrustTransactionId = evrotrustTransactionId,
            )
        }.onSuccess { response ->
            logDebug("checkSignedDocumentStatus onSuccess", TAG)
            emit(ResultEmittedData.success(documentStatusResponseMapper.map(response)))
        }.onRetry {
            logDebug("checkSignedDocumentStatus onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("checkSignedDocumentStatus onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)


    override fun checkDeliveredDocumentStatus(evrotrustThreadId: String): Flow<ResultEmittedData<DocumentStatusModel>> =
        flow {
            logDebug("checkDeliveredDocumentStatus", TAG)
            emit(ResultEmittedData.loading(null))
            getResult {
                documentsApi.checkDeliveredDocumentStatus(
                    evrotrustThreadId = evrotrustThreadId,
                )
            }.onSuccess { response ->
                logDebug("checkDeliveredDocumentStatus onSuccess", TAG)
                emit(ResultEmittedData.success(documentStatusResponseMapper.map(response)))
            }.onRetry {
                logDebug("checkDeliveredDocumentStatus onRetry", TAG)
                emit(ResultEmittedData.retry(null))
            }.onFailure {
                logError("checkDeliveredDocumentStatus onFailure", it, TAG)
                emit(ResultEmittedData.error(it, null))
            }

        }.flowOn(Dispatchers.IO)

    override fun requestIdentity(
        personalIdentificationNumber: String,
        language: String,
    ): Flow<ResultEmittedData<DocumentModel>> = flow {
        logDebug("requestIdentity", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            documentsApi.requestIdentity(
                personalIdentificationNumber = personalIdentificationNumber,
                language = language,
            )
        }.onSuccess {
            logDebug("requestIdentity onSuccess", TAG)
            emit(ResultEmittedData.success(documentResponseMapper.map(it)))
        }.onRetry {
            logDebug("requestIdentity onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("requestIdentity onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

    override fun authenticateDocument(
        data: DocumentAuthenticationRequestModel, evrotrustTransactionId: String
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("authenticateDocument", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            documentsApi.authenticateDocument(
                evrotrustTransactionId = evrotrustTransactionId,
                requestBody = documentAuthenticationRequestBodyMapper.map(data),
            )
        }.onSuccess {
            logDebug("authenticateDocument onSuccess", TAG)
            emit(ResultEmittedData.success(Unit))
        }.onRetry {
            logDebug("authenticateDocument onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("authenticateDocument onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

}
