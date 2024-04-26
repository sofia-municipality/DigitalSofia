/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.models.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
sealed class LoadingState : Parcelable, Serializable {

    data class Loading(
        val message: String? = null,
    ) : LoadingState()

    data object Ready : LoadingState()

}