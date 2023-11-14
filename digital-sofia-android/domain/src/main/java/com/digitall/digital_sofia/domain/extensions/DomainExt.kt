package com.digitall.digital_sofia.domain.extensions

/**
 * Use this type when the Use Case is should not receive
 * any arguments. This is more readable way.
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

typealias WithoutParams = Any?

/**
 * Use this function to execute Use Case without parameters.
 */
fun withoutParams(): WithoutParams = null