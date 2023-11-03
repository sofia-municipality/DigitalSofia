package com.digitall.digital_sofia.models.common

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class DatePickerConfig(
    val titleRes: Int,
    val minDate: Long,
    val maxDate: Long,
    val openAtDate: Long,
)