/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.utils

import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.usecase.documents.DocumentsUpdateUseCase
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UpdateDocumentsHelper(
    private val documentsUpdateUseCase: DocumentsUpdateUseCase,
) {

    companion object {
        private const val TAG = "UpdateDataHelperTag"
        private const val DOCUMENTS_UPDATE_TIMEOUT_MILLISECONDS = 10000L
    }

    private val _showMessageLiveData = SingleLiveEvent<Message>()
    val showBannerMessageLiveData = _showMessageLiveData.readOnly()

    private val _newAuthorizationEventLiveData = SingleLiveEvent<Unit>()
    val newAuthorizationEventLiveData = _newAuthorizationEventLiveData.readOnly()

    private var updateDocumentsJob: Job? = null

    @Volatile
    private var inProgress = false

    private var lastUpdateTimeStamp: Long = 0L

    fun startUpdateData(
        viewModelScope: CoroutineScope,
        isAuthorizationWithAppUpdateEnabled: Boolean
    ) {
        logDebug("startUpdateDocuments", TAG)
        updateDocumentsJob?.cancel()
        updateDocumentsJob = viewModelScope.launch {
            inProgress = true
            while (inProgress) {
                val now = System.currentTimeMillis()
                if (lastUpdateTimeStamp == 0L ||
                    lastUpdateTimeStamp + DOCUMENTS_UPDATE_TIMEOUT_MILLISECONDS < now
                ) {
                    lastUpdateTimeStamp = now
                    documentsUpdateUseCase.invoke().onEach { result ->
                        result.onLoading {
                            logDebug("updateDocuments onLoading", TAG)
                        }.onSuccess {
                            logDebug("updateDocuments onSuccess", TAG)
                            lastUpdateTimeStamp = now
                        }.onFailure {
                            logError("updateDocuments onFailure", it, TAG)
                            _showMessageLiveData.setValueOnMainThread(Message.error(R.string.error_server_error))
                            lastUpdateTimeStamp = now
                        }
                    }.launchInScope(viewModelScope)
                }
                delay(DOCUMENTS_UPDATE_TIMEOUT_MILLISECONDS)
            }
        }
    }

    fun stopUpdateData() {
        logDebug("stopUpdateDocuments", TAG)
        inProgress = false
        updateDocumentsJob?.cancel()
    }

}