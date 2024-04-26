/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.common

import com.digital.sofia.domain.models.common.AppLanguage
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.models.common.AppThemeType
import com.digital.sofia.domain.models.common.PinCode
import com.digital.sofia.domain.models.firebase.FirebaseTokenModel
import com.digital.sofia.domain.models.token.AccessTokenModel
import com.digital.sofia.domain.models.token.RefreshTokenModel
import com.digital.sofia.domain.models.user.UserModel

interface PreferencesRepository {

    fun getDeviceId(): String

    fun saveAccessToken(value: AccessTokenModel?)

    fun readAccessToken(): AccessTokenModel?

    fun saveFirebaseToken(value: FirebaseTokenModel?)

    fun readFirebaseToken(): FirebaseTokenModel?

    fun saveRefreshToken(value: RefreshTokenModel?)

    fun readRefreshToken(): RefreshTokenModel?

    fun saveDatabaseKey(value: String?)

    fun readDatabaseKey(): String?

    fun saveServerPublicKey(value: String?)

    fun readServerPublicKey(): String?

    fun saveAppThemeType(value: AppThemeType)

    fun readAppThemeType(): AppThemeType

    fun savePinCode(value: PinCode?)

    fun readPinCode(): PinCode?

    fun saveCurrentLanguage(value: AppLanguage)

    fun readCurrentLanguage(): AppLanguage

    fun saveUser(value: UserModel?)

    fun readUser(): UserModel?

    fun saveAppStatus(value: AppStatus?)

    fun readAppStatus(): AppStatus

    fun logoutFromPreferences()

}