/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.base

import com.google.gson.annotations.SerializedName

data class ErrorResponse (
    val type: String?,
    val message: String?,
    val error: String?,
    @SerializedName("error_description") val errorDescription: String?
)