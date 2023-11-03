package com.digitall.digital_sofia.models.common

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import com.digitall.digital_sofia.utils.CurrentContext
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Parcelize
sealed class StringSource : Parcelable, Serializable {

    data class Text(val text: String) : StringSource()

    data class Res(@StringRes val resId: Int) : StringSource()

    fun getString(context: Context): String {
        return when (this) {
            is Text -> text
            is Res -> context.getString(resId)
        }
    }

    fun getString(context: CurrentContext): String {
        return when (this) {
            is Text -> text
            is Res -> context.getString(resId)
        }
    }
}