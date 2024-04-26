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
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

class DocumentsSendSignedUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
) {

    companion object {
        private const val TAG = "DocumentsSendSignedUseCaseTag"
    }

    fun invoke(
        evrotrustTransactionId: String
    ): Flow<ResultEmittedData<Unit>> {
        logDebug("sendDocument evrotrustTransactionId: $evrotrustTransactionId", TAG)
        return documentsNetworkRepository.sendOpenedDocument(
            evrotrustTransactionId = evrotrustTransactionId,
        )
    }

}