package com.digitall.digital_sofia.models.common

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Parcelize
data class StartDestination(
    @IdRes val destination: Int,

    /**
     * Use this variable to pass arguments to the start destination of
     * the [destination] graph.
     */
    val arguments: Bundle? = null
) : Parcelable