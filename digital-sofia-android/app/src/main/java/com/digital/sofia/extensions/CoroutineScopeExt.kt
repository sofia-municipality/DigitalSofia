package com.digital.sofia.extensions

import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun CoroutineScope.launchWithDispatcher(
    block: suspend CoroutineScope.() -> Unit,
): Job {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError(
            "launchWithDispatcher Exception: ${throwable.message}",
            throwable,
            "launchWithDispatcher"
        )
    }
    return launch(Dispatchers.Default + exceptionHandler) {
        block()
    }
}