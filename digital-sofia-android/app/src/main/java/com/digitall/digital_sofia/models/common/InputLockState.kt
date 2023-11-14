package com.digitall.digital_sofia.models.common

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class InputLockState(
    val state: LockState,
    var timeLeftMilliseconds: Long?,
)

enum class LockState {
    LOCKED,
    UNLOCKED,
}