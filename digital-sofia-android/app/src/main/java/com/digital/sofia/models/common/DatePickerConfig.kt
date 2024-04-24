/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.models.common

data class DatePickerConfig(
    val titleRes: Int,
    val minDate: Long,
    val maxDate: Long,
    val openAtDate: Long,
)