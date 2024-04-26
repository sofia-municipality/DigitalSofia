/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.base

class EmptyResponse : BaseResponse() {
    override val type: String? = null
    override val message: String? = null
}