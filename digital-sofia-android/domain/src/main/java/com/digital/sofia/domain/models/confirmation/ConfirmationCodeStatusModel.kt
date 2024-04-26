package com.digital.sofia.domain.models.confirmation

data class ConfirmationCodeStatusModel(
    val code: String?,
    val expiresIn: Long?,
    val codeExists: Boolean?,
)