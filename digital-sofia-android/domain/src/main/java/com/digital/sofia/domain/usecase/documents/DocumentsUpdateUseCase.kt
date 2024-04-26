/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class DocumentsUpdateUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
    private val documentsDatabaseRepository: DocumentsDatabaseRepository,
) {

    companion object {
        private const val TAG = "UpdateDocumentsUseCaseTag"
    }

    private var cursor: String? = null

    private var flow: FlowCollector<ResultEmittedData<Unit>>? = null

    private val documents = mutableListOf<DocumentModel>()

    fun invoke(): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("updateDocuments", TAG)
        flow = this
        update()
    }.flowOn(Dispatchers.IO)

    private suspend fun update() {
        documentsNetworkRepository.getDocuments(cursor).onEach { result ->
            result.onLoading {
                logDebug("update onLoading", TAG)
                flow?.emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("update onSuccess", TAG)
                documents.addAll(it.documents)
                cursor = it.cursor
                if (it.cursor.isNullOrEmpty()) {
                    documentsDatabaseRepository.saveDocuments(documents)
                    flow?.emit(ResultEmittedData.success(Unit))
                } else {
                    if (flow != null) {
                        update()
                    }
                }
            }.onFailure {
                logError("update onFailure", it, TAG)
                flow?.emit(ResultEmittedData.error(it, null))
            }
        }.collect()
    }

}