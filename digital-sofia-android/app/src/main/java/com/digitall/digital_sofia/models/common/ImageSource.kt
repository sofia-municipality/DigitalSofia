package com.digitall.digital_sofia.models.common

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

sealed interface ImageSource : Parcelable {

    @Parcelize
    data class Url(
        val url: String?,
        @DrawableRes val placeholder: Int,
        @DrawableRes val error: Int,
    ) : ImageSource

    @Parcelize
    data class Res(
        @DrawableRes val res: Int,
    ) : ImageSource

}