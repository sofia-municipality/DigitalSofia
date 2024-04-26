/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.models.forms

import android.os.Parcelable
import com.digital.sofia.extensions.equalTo
import kotlinx.parcelize.Parcelize

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
