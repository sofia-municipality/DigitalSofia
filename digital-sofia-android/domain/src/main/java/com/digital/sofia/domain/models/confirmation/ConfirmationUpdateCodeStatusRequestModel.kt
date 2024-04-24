package com.digital.sofia.domain.models.confirmation

data class ConfirmationUpdateCodeStatusRequestModel (
    val code: String,
    val status: String,
)
