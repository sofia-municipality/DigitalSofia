package com.digital.sofia.domain.models.confirmation

data class ConfirmationGenerateCodeModel(
    val expiresIn: String?,
    val codeAlreadySent: Boolean?,
)
