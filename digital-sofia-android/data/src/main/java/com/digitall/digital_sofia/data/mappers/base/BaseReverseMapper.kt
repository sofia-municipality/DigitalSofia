package com.digitall.digital_sofia.data.mappers.base

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseReverseMapper<From, To> : BaseMapper<From, To>() {

    abstract fun reverse(to: To): From

    fun reverseList(tos: List<To>): List<From> {
        return tos.mapTo(ArrayList(tos.size), this::reverse)
    }

}