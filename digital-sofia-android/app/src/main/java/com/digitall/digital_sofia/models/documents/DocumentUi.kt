package com.digitall.digital_sofia.models.documents

import android.os.Parcelable
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel
import com.digitall.digital_sofia.extensions.equalTo
import kotlinx.parcelize.Parcelize

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Parcelize
class DocumentUi(
    val type: String,
    val signed: String,
    val expired: String,
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
            { status },
            { signed },
            { created },
            { expired },
            { fileUrl },
            { formioId },
            { fileName },
            { applicationId },
            { evrotrustTransactionId },
        )
    }

}