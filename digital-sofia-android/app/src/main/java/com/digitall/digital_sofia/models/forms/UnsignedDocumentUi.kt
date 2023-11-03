package com.digitall.digital_sofia.models.forms

import android.os.Parcelable
import com.digitall.digital_sofia.extensions.equalTo
import kotlinx.parcelize.Parcelize

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Parcelize
data class UnsignedDocumentUi(
    val evrotrustTransactionId: String,
    val title: String,
) : HomeAdapterMarker, Parcelable {

    override fun isItemSame(other: Any?): Boolean {
        return equalTo(other)
    }

    override fun isContentSame(other: Any?): Boolean {
        return equalTo(
            other,
            { evrotrustTransactionId },
            { title },
        )
    }

}
