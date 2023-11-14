package com.digitall.digital_sofia.domain.extensions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

fun <T> MutableStateFlow<T>.readOnly(): StateFlow<T> = this