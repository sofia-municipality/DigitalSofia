package com.digitall.digital_sofia.domain.models.documents

import com.digitall.digital_sofia.domain.models.base.TypeEnum
import com.digitall.digital_sofia.domain.models.common.TEXT_UNKNOWN
import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class DocumentModel(
    val type: String?,
    val signed: String?,
    val created: String?,
    val fileUrl: String?,
    val expired: String?,
    val rejected: String?,
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
}

// unsigned
//
//failed
//
//expired
//
//rejected
//
//signed

//1 - Чакащо
//2 - Подписано
//3 - Отхвърлено
//4 - Изтекло
//5 - Неуспешно
//6 - Изтеглено
//7 - Недоставено
//8 - Неуспешно разпознаване на лице
//9 - В изчакване

//1 - Pending
//2 - Signed
//3 - Rejected
//4 - Expired
//5 - Failed
//6 - Withdrawn
//7 - Undeliverable
//8 - Failed face recognition
//9 - On hold