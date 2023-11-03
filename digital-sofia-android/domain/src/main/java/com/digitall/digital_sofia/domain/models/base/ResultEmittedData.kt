package com.digitall.digital_sofia.domain.models.base

/**
 * A generic class that holds a value with its loading status.
 *
 * Result is usually created by the Repository classes where they return
 * `LiveData<Result<T>>` to pass back the latest data to the UI with its fetch status.
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

data class ResultEmittedData<out T>(
    val status: Status,
    val data: T?,
    val error: Error?
) {
    data class Error(
        val responseCode: Int,
        val serverType: String?,
        val serverMessage: String?,
        val responseMessage: String?,
    ) {
    }

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING,
        NOTHING
    }

    companion object {
        fun <T> success(data: T): ResultEmittedData<T> =
            ResultEmittedData(Status.SUCCESS, data, null)

        fun <T> loading(data: T? = null): ResultEmittedData<T> =
            ResultEmittedData(Status.LOADING, data, null)

        fun <T> nothing(data: T? = null): ResultEmittedData<T> =
            ResultEmittedData(Status.NOTHING, data, null)

        fun <T> error(error: Error, data: T? = null): ResultEmittedData<T> =
            ResultEmittedData(Status.ERROR, data, error)
    }
}

inline fun <T : Any> ResultEmittedData<T>.onSuccess(action: (T) -> Unit): ResultEmittedData<T> {
    if (status == ResultEmittedData.Status.SUCCESS && data != null) action(data)
    return this
}

inline fun <T : Any> ResultEmittedData<T>.onFailure(action: (ResultEmittedData.Error) -> Unit): ResultEmittedData<T> {
    if (status == ResultEmittedData.Status.ERROR && error != null) action(error)
    return this
}

inline fun <T : Any> ResultEmittedData<T>.onLoading(action: () -> Unit): ResultEmittedData<T> {
    if (status == ResultEmittedData.Status.LOADING) action()
    return this
}

inline fun <T : Any> ResultEmittedData<T>.onNothing(action: () -> Unit): ResultEmittedData<T> {
    if (status == ResultEmittedData.Status.NOTHING) action()
    return this
}