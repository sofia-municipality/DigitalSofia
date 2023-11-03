package com.digitall.digital_sofia.data.mappers.base

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseMapper<From, To> {

    abstract fun map(from: From): To

    open fun mapList(fromList: List<From>): List<To> {
        return fromList.mapTo(ArrayList(fromList.size), this::map)
    }

}