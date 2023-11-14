package com.digitall.digital_sofia.utils

import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.StringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class UpdateDocumentsHelper(
    private val documentsUseCase: DocumentsUseCase,
    private val notificationHelper: NotificationHelper,
) {

    companion object {
        private const val TAG = "UpdateDocumentsHelperTag"
        private const val DOCUMENTS_UPDATE_TIMEOUT_MILLISECONDS = 30000L
    }

    private val _showBannerMessageLiveData = SingleLiveEvent<BannerMessage>()
    val showBannerMessageLiveData = _showBannerMessageLiveData.readOnly()

    private var updateDocumentsJob: Job? = null

    @Volatile
    private var inProgress = false

    private var lastUpdateTimeStamp: Long = 0L

    fun startUpdateDocuments(
        viewModelScope: CoroutineScope,
    ) {
        logDebug("startUpdateDocuments", TAG)
        updateDocumentsJob?.cancel()
        updateDocumentsJob = viewModelScope.launch {
            documentsUseCase.subscribeToUnsignedDocuments().onEach {
                if (it.isEmpty()) {
                    logDebug("subscribeToDocuments isEmpty", TAG)
                    notificationHelper.hideAllNotifications()
                } else {
                    logDebug("subscribeToDocuments isNotEmpty", TAG)
                    notificationHelper.showNotification(
                        title = StringSource.Text("Document to sign"),
                        content = StringSource.Text("Number of documents: ${it.size}"),
                    )
                }
            }.launch(viewModelScope)
            inProgress = true
            while (inProgress) {
                val now = System.currentTimeMillis()
                if (lastUpdateTimeStamp == 0L ||
                    lastUpdateTimeStamp + DOCUMENTS_UPDATE_TIMEOUT_MILLISECONDS < now
                ) {
                    lastUpdateTimeStamp = now
                    documentsUseCase.updateDocuments().onEach { result ->
                        result.onLoading {
                            logDebug("updateDocuments onLoading", TAG)
                        }.onSuccess {
                            logDebug("updateDocuments onSuccess", TAG)
                            lastUpdateTimeStamp = now
                        }.onFailure {
                            logError("updateDocuments onFailure", TAG)
                            _showBannerMessageLiveData.value =
                                BannerMessage.error("Error update documents")
                            lastUpdateTimeStamp = now
                        }
                    }.launch(viewModelScope)
                }
                delay(DOCUMENTS_UPDATE_TIMEOUT_MILLISECONDS)
            }
        }
    }

    fun stopUpdateDocuments() {
        logDebug("stopUpdateDocuments", TAG)
        inProgress = false
        updateDocumentsJob?.cancel()
    }

}