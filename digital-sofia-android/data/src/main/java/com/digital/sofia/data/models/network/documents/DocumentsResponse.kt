/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.documents

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class DocumentsResponse(
    @SerializedName("total") val total: Int?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
    @SerializedName("documents") val documents: List<DocumentResponse>?,
    @SerializedName("pagination") val pagination: DocumentsPaginationResponse?,
) : BaseResponse()

data class DocumentsPaginationResponse(
    @SerializedName("cursor") val cursor: String?,
)

data class DocumentResponse(
    @SerializedName("type") override val type: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("signed") val signed: String?,
    @SerializedName("expired") val expired: String?,
    @SerializedName("fileUrl") val fileUrl: String?,
    @SerializedName("created") val created: String?,
    @SerializedName("rejected") val rejected: String?,
    @SerializedName("formioId") val formioId: String?,
    @SerializedName("modified") val modified: String?,
    @SerializedName("fileName") val fileName: String?,
    @SerializedName("validUntill") val validUntill: String?,
    @SerializedName("applicationId") val applicationId: String?,
    @SerializedName("referenceNumber") val referenceNumber: String?,
    @SerializedName("evrotrustThreadId") val evrotrustThreadId: String?,
    @SerializedName("evrotrustTransactionId") val evrotrustTransactionId: String?,
    @SerializedName("message") override val message: String?,
): BaseResponse()
