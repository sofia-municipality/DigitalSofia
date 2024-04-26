package com.digital.sofia.domain.models.firebase

import java.io.Serializable

data class FirebaseTokenModel(
    val isSend: Boolean,
    val token: String,
) : Serializable {}
