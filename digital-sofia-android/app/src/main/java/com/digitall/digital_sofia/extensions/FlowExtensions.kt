package com.digitall.digital_sofia.extensions

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

fun <T> Flow<T>.launch(scope: LifecycleCoroutineScope) {
    scope.launchWhenResumed { this@launch.flowOn(Dispatchers.Main).collect() }
}

fun <T> Flow<T>.launch(scope: CoroutineScope) {
    scope.launch { this@launch.flowOn(Dispatchers.IO).collect() }
}

fun <T> Flow<T>.launchInJob(scope: CoroutineScope): Job {
    return scope.launch(Dispatchers.IO) {
        this@launchInJob.collect()
    }
}