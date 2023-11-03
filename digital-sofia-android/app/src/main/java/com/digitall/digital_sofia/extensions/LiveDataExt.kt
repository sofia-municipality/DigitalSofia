package com.digitall.digital_sofia.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

fun <T> MutableLiveData<T>.readOnly(): LiveData<T> = this

fun MutableLiveData<Unit>.call() {
    this.value = Unit
}

