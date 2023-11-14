package com.digitall.digital_sofia.domain.usecase.documents

import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel
import com.digitall.digital_sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digitall.digital_sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface DocumentsUseCase {

    fun getDocuments(): Flow<ResultEmittedData<List<DocumentModel>>>

    fun updateDocuments(): Flow<ResultEmittedData<Unit>>

    fun subscribeToDocuments(): Flow<List<DocumentModel>>

    fun subscribeToUnsignedDocuments(): Flow<List<DocumentModel>>

}

class DocumentsUseCaseImpl(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
    private val documentsDatabaseRepository: DocumentsDatabaseRepository,
) : DocumentsUseCase {

    companion object {
        private const val TAG = "GetDocumentsUseCaseTag"
    }

    override fun getDocuments(): Flow<ResultEmittedData<List<DocumentModel>>> = flow {
        logDebug("getDocuments", TAG)
        documentsNetworkRepository.getDocuments().onEach { result ->
            result.onLoading {
                logDebug("getDocuments onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("getDocuments onSuccess", TAG)
                documentsDatabaseRepository.saveDocuments(it)
                emit(ResultEmittedData.success(it))
            }.onFailure { documentError ->
                logError("getDocuments onFailure", TAG)
                emit(ResultEmittedData.error(documentError, null))
            }
        }.collect()
    }

    override fun updateDocuments(): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("updateDocuments", TAG)
        documentsNetworkRepository.getDocuments().onEach { result ->
            result.onLoading {
                logDebug("updateDocuments onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("updateDocuments onSuccess", TAG)
                documentsDatabaseRepository.saveDocuments(it)
                emit(ResultEmittedData.success(Unit))
            }.onFailure { documentError ->
                logError("updateDocuments onFailure", TAG)
                emit(ResultEmittedData.error(documentError, null))
            }
        }.collect()
    }

    override fun subscribeToDocuments(): Flow<List<DocumentModel>> {
        logDebug("subscribeToDocuments", TAG)
        return documentsDatabaseRepository.subscribeToDocuments()
    }

    override fun subscribeToUnsignedDocuments(): Flow<List<DocumentModel>> {
        logDebug("subscribeToUnsignedDocuments", TAG)
        return documentsDatabaseRepository.subscribeToDocumentsWithStatus(DocumentStatusModel.SIGNING)
    }

}

