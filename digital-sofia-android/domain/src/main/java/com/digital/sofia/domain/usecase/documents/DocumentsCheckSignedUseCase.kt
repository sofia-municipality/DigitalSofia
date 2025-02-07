/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class DocumentsCheckSignedUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
) {

    companion object {
        private const val TAG = "DocumentsSendSignedUseCaseTag"
        private const val CHECK_STATUS_INTERVAL_DELAY = 10000L
        private const val CHECK_STATUS_REQUEST_TIMEOUT = 120000L
    }

    @Volatile
    private var isCheckStatusPollingEnabled = true

    fun invoke(
        evrotrustTransactionId: String
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("checkSignedDocumentStatus evrotrustTransactionId: $evrotrustTransactionId", TAG)
        checkStatus(transactionId = evrotrustTransactionId, flow = this@flow)
    }.flowOn(Dispatchers.IO)

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun checkStatus(
        transactionId: String,
        flow: FlowCollector<ResultEmittedData<Unit>>,
    ) {
        logDebug("checkStatus", TAG)
        isCheckStatusPollingEnabled = true
        val checkStatusStartTime = System.currentTimeMillis()
        flow.emit(ResultEmittedData.loading(data = null))
        channelFlow {
            while (!isClosedForSend) {
                if (!isCheckStatusPollingEnabled) {
                    close()
                    return@channelFlow
                }

                delay(CHECK_STATUS_INTERVAL_DELAY)
                if (isCheckStatusPollingEnabled.not()) return@channelFlow
                send(documentsNetworkRepository.checkSignedDocumentStatus(evrotrustTransactionId = transactionId))
            }
        }.collect {
            it.collect { result ->
                result.onSuccess { model ->
                    logDebug(
                        "checkStatus onSuccess status: $model",
                        TAG
                    )
                    when(model) {
                        DocumentStatusModel.SIGNED,
                        DocumentStatusModel.REJECTED -> {
                            isCheckStatusPollingEnabled = false
                            flow.emit(ResultEmittedData.success(Unit))
                        }
                        else -> {
                            logError("checkStatus onSuccess else", TAG)
                            if (System.currentTimeMillis() - checkStatusStartTime >= CHECK_STATUS_REQUEST_TIMEOUT) {
                                isCheckStatusPollingEnabled = false
                                flow.emit(
                                    ResultEmittedData.error(
                                        error = ResultEmittedData.Error(
                                            responseCode = 200,
                                            serverMessage = "Check signing timeout reached",
                                            responseMessage = "Check signing timeout reached",
                                            serverType = null,
                                        ),
                                    )
                                )
                            }
                        }
                    }
                }.onRetry {
                    checkStatus(transactionId = transactionId, flow = flow)
                }.onFailure { failure ->
                    logError("checkStatus onFailure", failure, TAG)
                    when {
                        failure.responseCode == 443 -> {}
                        else -> {
                            isCheckStatusPollingEnabled = false
                            flow.emit(
                                ResultEmittedData.error(error = failure)
                            )
                        }
                    }
                }
            }
        }
    }
}