/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.base

import okhttp3.ResponseBody

abstract class BaseResponse  {
    abstract val type: String?
    abstract val message: String?
}