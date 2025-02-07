package com.digital.sofia.ui.fragments.main.documents.list

import androidx.paging.PageKeyedDataSource
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.usecase.documents.DocumentsGetHistoryUseCase
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.mappers.documents.DocumentsUiMapper
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlin.concurrent.Volatile

class DocumentsDataSource(
    private val showLoader: () -> Unit,
    private val hideLoader: () -> Unit,
    private val viewModelScope: CoroutineScope,
    private val documentsMapper: DocumentsUiMapper,
    private val documentsGetHistoryUseCase: DocumentsGetHistoryUseCase,
    private val showErrorMessage: () -> Unit,
) : PageKeyedDataSource<String, DocumentsAdapterMarker>() {

    companion object {
        private const val TAG = "DocumentsDataSourceTag"
        private const val DELAY_500 = 500L
    }

    @Volatile
    private var cursor: String? = null

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, DocumentsAdapterMarker>
    ) {
        logDebug("loadInitial", TAG)
        showLoader.invoke()
        loadNext(withHeader = true) {
            logDebug("loadInitial onResult size: ${it.size}", TAG)
            callback.onResult(
                it,
                null,
                cursor
            )
        }
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, DocumentsAdapterMarker>
    ) {
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, DocumentsAdapterMarker>
    ) {
        logDebug("loadRange", TAG)
        loadNext(withHeader = false) {
            logDebug("loadRange onResult size: ${it.size}", TAG)
            callback.onResult(it, cursor)
        }
    }

    private fun loadNext(withHeader: Boolean, callback: (List<DocumentsAdapterMarker>) -> Unit) {
        documentsGetHistoryUseCase.invoke(cursor = cursor).onEach { result ->
            result.onLoading {
                logDebug("loadNext onLoading", TAG)
            }.onSuccess { model ->
                logDebug("loadNext onSuccess", TAG)
                if (model.documents.isNotEmpty()) {
                    cursor = model.cursor
                    callback(
                        if (withHeader)
                            documentsMapper.mapWithHeader(model.documents)
                        else documentsMapper.map(model.documents)
                    )
                }
                delay(DELAY_500)
                hideLoader.invoke()
            }.onRetry {
                loadNext(withHeader = withHeader, callback = callback)
            }.onFailure { failure ->
                logError("loadNext onFailure", failure, TAG)
                showErrorMessage.invoke()
                hideLoader.invoke()
            }
        }.launchInScope(viewModelScope)
    }

}