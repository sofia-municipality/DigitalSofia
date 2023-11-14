package com.digitall.digital_sofia.data.models.network.forms

import com.digitall.digital_sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class FormsResponse(
    @SerializedName("forms") val forms: List<FormResponse>,
    @SerializedName("totalCount") val total: Int?,
    @SerializedName("pageNo") val pageNo: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()

data class FormResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("formId") val formId: String?,
    @SerializedName("formName") val formName: String?,
    @SerializedName("processKey") val processKey: String?,
)
