/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.extensions

import androidx.fragment.app.Fragment
import com.digital.sofia.models.common.DatePickerConfig
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker

fun Fragment.showDataPicker(
    datePickerConfig: DatePickerConfig,
    onPositiveClick: (Long) -> Unit,
) {
    val picker = MaterialDatePicker.Builder.datePicker().also { builder ->
        builder.setTitleText(getString(datePickerConfig.titleRes))
        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
        builder.setCalendarConstraints(
            CalendarConstraints.Builder()
                .setValidator(
                    CompositeDateValidator.allOf(
                        listOf(
                            DateValidatorPointForward.from(datePickerConfig.minDate),
                            DateValidatorPointBackward.before(datePickerConfig.maxDate)
                        )
                    )
                )
                .setStart(datePickerConfig.minDate)
                .setEnd(datePickerConfig.maxDate)
                .setOpenAt(datePickerConfig.openAtDate)
                .build()
        )
        datePickerConfig.openAtDate.also(builder::setSelection)
    }.build()
    picker.addOnPositiveButtonClickListener(onPositiveClick::invoke)
    picker.show(childFragmentManager, "")
}