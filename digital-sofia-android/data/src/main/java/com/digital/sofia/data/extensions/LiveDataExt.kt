package com.digital.sofia.data.extensions

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
fun <T> MutableLiveData<T>.readOnly(): LiveData<T> = this

@MainThread
fun MutableLiveData<Unit>.call() {
    this.value = Unit
}

fun MutableLiveData<Unit>.callOnMainThread() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        value = null
    } else {
        Handler(Looper.getMainLooper()).post {
            value = null
        }
    }
}