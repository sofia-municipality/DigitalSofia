package com.digital.sofia.models.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
sealed class NetworkState : Parcelable, Serializable {

    data object Disconnected : NetworkState()

    data object Connected : NetworkState()
}

