/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.mappers.common

import com.digital.sofia.R
import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.authorization.AuthCreatePassCodeResponseErrorCodes
import com.digital.sofia.utils.CurrentContext
import com.evrotrust.lib.EvrotrustSDK

class CreateCodeResponseErrorToStringMapper(
    private val currentContext: CurrentContext,
) : BaseMapper<AuthCreatePassCodeResponseErrorCodes, String>() {

    override fun map(from: AuthCreatePassCodeResponseErrorCodes): String {
        return currentContext.get().getString(
            when (from) {
                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_CONTAINS_PHONE_NUMBER ->
                    R.string.create_pin_error_easy

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_IS_SEQUENT_DIGITS ->
                    R.string.create_pin_error_easy

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_REPEATED_DIGITS ->
                    R.string.create_pin_error_easy

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_DATE_OF_BIRTH ->
                    R.string.create_pin_error_easy

                AuthCreatePassCodeResponseErrorCodes.PASS_CODE_ALREADY_USED ->
                    R.string.create_pin_error_easy
            }
        )
    }
}