package com.digitall.digital_sofia.domain.repository.common

import com.digitall.digital_sofia.domain.models.common.AppLanguage
import com.digitall.digital_sofia.domain.models.common.AppStatus
import com.digitall.digital_sofia.domain.models.common.AppThemeType
import com.digitall.digital_sofia.domain.models.common.PinCode
import com.digitall.digital_sofia.domain.models.user.UserModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface PreferencesRepository {

    fun getDeviceId(): String

    fun saveAccessToken(value: String)

    fun readAccessToken(): String?

    fun saveFirebaseToken(value: String?)

    fun readFirebaseToken(): String?

    fun saveRefreshToken(value: String?)

    fun readRefreshToken(): String?

    fun saveDatabaseKey(value: String)

    fun readDatabaseKey(): String?

    fun saveServerPublicKey(value: String)

    fun readServerPublicKey(): String?

    fun saveAppThemeType(value: AppThemeType)

    fun readAppThemeType(): AppThemeType

    fun savePinCode(value: PinCode)

    fun readPinCode(): PinCode?

    fun saveCurrentLanguage(value: AppLanguage)

    fun readCurrentLanguage(): AppLanguage

    fun saveUser(value: UserModel)

    fun readUser(): UserModel?

    fun saveAppStatus(value: AppStatus)

    fun readAppStatus(): AppStatus

    fun saveEvrotrustTransactionIdForLogin(value: String)

    fun readEvrotrustTransactionIdForLogin(): String?

    fun logoutFromPreferences()

}