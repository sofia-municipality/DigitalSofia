package com.digital.sofia.models.common

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ProfileVerificationType : Parcelable {
    data object ProfileVerificationReady : ProfileVerificationType()
    data object ProfileVerificationRejected : ProfileVerificationType()
    data class ProfileVerificationError(@StringRes var errorMessageRes: Int?): ProfileVerificationType()
}