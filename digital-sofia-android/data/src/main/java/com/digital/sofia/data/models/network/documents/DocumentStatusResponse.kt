/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.documents

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class DocumentStatusResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()