package com.digitall.digital_sofia.mappers.common

import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.data.mappers.base.BaseMapper
import com.digitall.digital_sofia.data.models.network.authorization.AuthCreatePassCodeResponseErrorCodes
import com.digitall.digital_sofia.utils.CurrentContext

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class CreateCodeResponseErrorToStringMapper(
    private val currentContext: CurrentContext,
) : BaseMapper<AuthCreatePassCodeResponseErrorCodes, String>() {

    override fun map(from: AuthCreatePassCodeResponseErrorCodes): String {
        return currentContext.get().getString(
            when (from) {
                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_CONTAINS_PHONE_NUMBER ->
                    R.string.create_pin_error_pass_code_your_part_phone_number

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_IS_SEQUENT_DIGITS ->
                    R.string.create_pin_error_pass_code_cannot_be_set_as_sequential_numbers

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_REPEATED_DIGITS ->
                    R.string.create_pin_error_pass_code_repeating_digits_more_than_2

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_DATE_OF_BIRTH ->
                    R.string.create_pin_error_pass_code_your_date_of_birth

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_ALREADY_USED ->
                    R.string.create_pin_error_pass_code_already_used
            }
        )
    }
}