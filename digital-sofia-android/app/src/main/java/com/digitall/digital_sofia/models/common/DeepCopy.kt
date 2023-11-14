package com.digitall.digital_sofia.models.common

/**
 * Simple interface to provide consistent method to markers and their descendants
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

fun interface DeepCopy<T> {
    fun deepCopy(): T
}