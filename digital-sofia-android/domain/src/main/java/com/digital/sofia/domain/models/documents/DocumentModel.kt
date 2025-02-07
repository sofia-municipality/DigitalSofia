/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.documents

import com.digital.sofia.domain.models.base.TypeEnum
import com.digital.sofia.domain.models.common.TEXT_UNKNOWN
import java.io.Serializable

data class DocumentModel(
    val type: String?,
    val signed: String?,
    val created: String?,
    val fileUrl: String?,
    val expired: String?,
    val rejected: String?,
    val generated: String?,
    val formioId: String?,
    val fileName: String?,
    val modified: String?,
    val validUntill: String?,
    val applicationId: String?,
    val referenceNumber: String?,
    val evrotrustThreadId: String?,
    val status: DocumentStatusModel,
    val evrotrustTransactionId: String,
) : Serializable

enum class DocumentStatusModel(override val type: String) : TypeEnum {
    UNKNOWN(TEXT_UNKNOWN),
    SIGNED("signed"),
    SIGNING("signing"),
    EXPIRED("expired"),
    REJECTED("rejected"),
    PENDING("pending"),
    UNSIGNED("unsigned"),
    FAILED("failed"),
    WITHDRAWN("withdrawn"),
    DELIVERING("delivering"),
    GENERATED("generated")
}