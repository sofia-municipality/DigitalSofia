/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.models.documents

import android.os.Parcelable
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.extensions.equalTo
import kotlinx.parcelize.Parcelize

@Parcelize
class DocumentUi(
    val type: String,
    val signed: String?,
    val expired: String?,
    val rejected: String?,
    val generated: String?,
    val fileUrl: String,
    val created: String,
    val formioId: String,
    val fileName: String,
    val applicationId: String,
    val status: DocumentStatusModel,
    val evrotrustTransactionId: String,
) : DocumentsAdapterMarker, Parcelable {

    override fun isItemSame(other: Any?): Boolean {
        return equalTo(other)
    }

    override fun isContentSame(other: Any?): Boolean {
        return equalTo(
            other,
            { type },
            { signed },
            { status },
            { created },
            { expired },
            { rejected },
            { generated },
            { fileUrl },
            { formioId },
            { fileName },
            { applicationId },
            { evrotrustTransactionId },
        )
    }

}