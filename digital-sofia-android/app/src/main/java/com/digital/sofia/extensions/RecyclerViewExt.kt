package com.digital.sofia.extensions

import androidx.recyclerview.widget.RecyclerView
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.utils.RecyclerViewAdapterDataObserver

private const val TAG = "RecyclerViewExtTag"

fun RecyclerView.Adapter<RecyclerView.ViewHolder>.registerChangeStateObserver(
    observer: RecyclerViewAdapterDataObserver,
    changeStateListener: (() -> Unit),
) {
    try {
        registerAdapterDataObserver(observer)
        observer.changeStateListener = changeStateListener
    } catch (e: Exception) {
        logError("registerChangeStateObserver Exception: ${e.message}", e, TAG)
    }
}

fun RecyclerView.Adapter<RecyclerView.ViewHolder>.unregisterChangeStateObserver(
    observer: RecyclerViewAdapterDataObserver,
) {
    try {
        if (hasObservers()) {
            unregisterAdapterDataObserver(observer)
        }
        observer.changeStateListener = null
    } catch (e: Exception) {
        logError("unregisterChangeStateObserver Exception: ${e.message}", e, TAG)
    }
}