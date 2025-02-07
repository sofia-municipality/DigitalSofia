package com.digital.sofia.ui.fragments.main.pending.list

import androidx.paging.PageKeyedDataSource
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.usecase.documents.DocumentsGetPendingUseCase
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.mappers.forms.PendingDocumentUiMapper
import com.digital.sofia.models.forms.PendingAdapterMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach

class PendingDataSource(
    private val showLoader: () -> Unit,
    private val hideLoader: () -> Unit,
    private val viewModelScope: CoroutineScope,
    private val pendingMapper: PendingDocumentUiMapper,
    private val documentsGetPendingUseCase: DocumentsGetPendingUseCase,
    private val showErrorMessage: () -> Unit,
) : PageKeyedDataSource<String, PendingAdapterMarker>() {

    companion object {
        private const val TAG = "PendingDataSourceTag"
        private const val DELAY_500 = 500L
    }

    @Volatile
    private var cursor: String? = null

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, PendingAdapterMarker>
    ) {
        logDebug("loadInitial", TAG)
        showLoader.invoke()
        loadNext {
            logDebug("loadRange onResult size: ${it.size}", TAG)
            callback.onResult(it, null, cursor)
        }
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, PendingAdapterMarker>
    ) {
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, PendingAdapterMarker>
    ) {
        logDebug("loadRange", TAG)
        loadNext {
            logDebug("loadRange onResult size: ${it.size}", TAG)
            callback.onResult(it, cursor)
        }
    }

    private fun loadNext(callback: (List<PendingAdapterMarker>) -> Unit) {
        documentsGetPendingUseCase.invoke(cursor = cursor).onEach { result ->
            result.onLoading {
                logDebug("loadNext onLoading", TAG)
            }.onSuccess { model ->
                logDebug("loadNext onSuccess", TAG)
                if (model.documents.isNotEmpty()) {
                    cursor = model.cursor
                    callback(pendingMapper.mapList(model.documents))
                }
                delay(DELAY_500)
                hideLoader.invoke()
            }.onRetry {
                loadNext(callback = callback)
            }.onFailure { failure ->
                logError("loadNext onFailure", failure, TAG)
                showErrorMessage.invoke()
                hideLoader.invoke()
            }
        }.launchInScope(viewModelScope)
    }
}