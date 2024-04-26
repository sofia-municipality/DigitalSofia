package com.digital.sofia.domain.models.documents

data class DocumentAuthenticationRequestModel(
    val pin: String,
    val email: String?,
    val phoneNumber: String?,
    val firebaseToken: String
)
