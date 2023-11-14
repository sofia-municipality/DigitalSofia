package com.digitall.digital_sofia.models.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Parcelize
sealed class LoadingState : Parcelable, Serializable {

    object Ready : LoadingState()

    object Loading : LoadingState()

}