package com.digitall.digital_sofia.data.repository.network.documents

import com.digitall.digital_sofia.data.mappers.network.documents.DocumentsResponseMapper
import com.digitall.digital_sofia.data.network.documents.DocumentsApi
import com.digitall.digital_sofia.data.repository.network.base.BaseRepository
import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsNetworkRepositoryImpl(
    private val documentsApi: DocumentsApi,
    private val dispatcherIO: CoroutineDispatcher,
    private val documentsResponseMapper: DocumentsResponseMapper,
) : DocumentsNetworkRepository, BaseRepository() {

    companion object {
        private const val TAG = "DocumentsNetworkRepositoryTag"
    }

    override fun getDocuments(): Flow<ResultEmittedData<List<DocumentModel>>> = flow {
        logDebug("getDocuments", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            documentsApi.getDocument()
        }.onSuccess {
            logDebug("getDocuments onSuccess", TAG)
            val result = documentsResponseMapper.map(it)
            emit(ResultEmittedData.success(result))
        }.onFailure {
            logError("getDocuments onFailure error code: ${it.responseCode}", TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(dispatcherIO)

}
