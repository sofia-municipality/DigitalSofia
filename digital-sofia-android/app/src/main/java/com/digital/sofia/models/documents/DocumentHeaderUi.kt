package com.digital.sofia.models.documents

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.digital.sofia.R
import com.digital.sofia.extensions.equalTo
import kotlinx.parcelize.Parcelize

@Parcelize
class DocumentHeaderUi(
    @DrawableRes var iconRes: Int = R.drawable.ic_header_documents,
    @StringRes val titleRes: Int = R.string.document_title
) : DocumentsAdapterMarker, Parcelable {
    override fun isItemSame(other: Any?): Boolean {
        return equalTo(other)
    }

    override fun isContentSame(other: Any?): Boolean {
        return equalTo(
            other,
            { iconRes },
            { titleRes }
        )
    }
}