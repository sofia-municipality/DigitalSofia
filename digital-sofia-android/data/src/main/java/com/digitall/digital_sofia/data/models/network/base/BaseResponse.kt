package com.digitall.digital_sofia.data.models.network.base

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseResponse {
    abstract val type: String?
    abstract val message: String?
}