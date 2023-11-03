package com.digitall.digital_sofia.data.repository.common

import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import com.digitall.digital_sofia.data.PROPERTY_KEY_ACCESS_TOKEN
import com.digitall.digital_sofia.data.PROPERTY_KEY_APP_STATUS
import com.digitall.digital_sofia.data.PROPERTY_KEY_APP_THEME_TYPE
import com.digitall.digital_sofia.data.PROPERTY_KEY_CURRENT_LANGUAGE
import com.digitall.digital_sofia.data.PROPERTY_KEY_DATABASE
import com.digitall.digital_sofia.data.PROPERTY_KEY_DEVICE_ID
import com.digitall.digital_sofia.data.PROPERTY_KEY_EVROTRUST_TRANSACTION_ID_FOR_LOGIN
import com.digitall.digital_sofia.data.PROPERTY_KEY_FIREBASE_TOKEN
import com.digitall.digital_sofia.data.PROPERTY_KEY_PIN_CODE
import com.digitall.digital_sofia.data.PROPERTY_KEY_REFRESH_TOKEN
import com.digitall.digital_sofia.data.PROPERTY_KEY_SERVER_PUBLIC
import com.digitall.digital_sofia.data.PROPERTY_KEY_USER
import com.digitall.digital_sofia.domain.extensions.getEnumTypeValue
import com.digitall.digital_sofia.domain.models.common.AppLanguage
import com.digitall.digital_sofia.domain.models.common.AppStatus
import com.digitall.digital_sofia.domain.models.common.AppThemeType
import com.digitall.digital_sofia.domain.models.common.PinCode
import com.digitall.digital_sofia.domain.models.user.UserModel
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.google.gson.Gson
import java.util.UUID

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

internal class PreferencesRepositoryImpl(
    private val preferences: EncryptedSharedPreferences,
) : PreferencesRepository {

    companion object {
        private const val TAG = "PreferencesRepositoryTag"
    }

    override fun getDeviceId(): String {
        val value = preferences.getString(PROPERTY_KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString()
            .also {
                preferences.edit().putString(PROPERTY_KEY_DEVICE_ID, it).apply()
            }
        logDebug("getDeviceId value: $value", TAG)
        return value
    }

    override fun saveAccessToken(value: String) {
        logDebug("saveAccessToken value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_ACCESS_TOKEN, value).apply()
    }

    override fun readAccessToken(): String? {
        val value = preferences.getString(PROPERTY_KEY_ACCESS_TOKEN, null)
        logDebug("readAccessToken value: $value", TAG)
        return value
    }

    override fun saveFirebaseToken(value: String?) {
        logDebug("saveFirebaseToken value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_FIREBASE_TOKEN, value).apply()
    }

    override fun readFirebaseToken(): String? {
        val value = preferences.getString(PROPERTY_KEY_FIREBASE_TOKEN, null)
        logDebug("readFirebaseToken value: $value", TAG)
        return value
    }

    override fun saveRefreshToken(value: String?) {
        logDebug("saveRefreshToken value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_REFRESH_TOKEN, value).apply()
    }

    override fun readRefreshToken(): String? {
        val value = preferences.getString(PROPERTY_KEY_REFRESH_TOKEN, null)
        logDebug("readRefreshToken value: $value", TAG)
        return value
    }

    override fun saveDatabaseKey(value: String) {
        logDebug("saveDatabaseKey value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_DATABASE, value).apply()
    }

    override fun readDatabaseKey(): String? {
        val value = preferences.getString(PROPERTY_KEY_DATABASE, null)
        logDebug("readDatabaseKey value: $value", TAG)
        return value
    }

    override fun saveServerPublicKey(value: String) {
        logDebug("saveServerPublicKey value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_SERVER_PUBLIC, value).apply()
    }

    override fun readServerPublicKey(): String? {
        val value = preferences.getString(PROPERTY_KEY_SERVER_PUBLIC, null)
        logDebug("readServerPublicKey value: $value", TAG)
        return value
    }

    override fun saveAppThemeType(value: AppThemeType) {
        logDebug("saveAppThemeType value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_APP_THEME_TYPE, value.type).apply()
    }

    override fun readAppThemeType(): AppThemeType {
        val value = getEnumTypeValue<AppThemeType>(
            preferences.getString(PROPERTY_KEY_APP_THEME_TYPE, AppThemeType.FOLLOW_SYSTEM.type)!!
        ) ?: AppThemeType.FOLLOW_SYSTEM
        logDebug("readAppThemeType value: $value", TAG)
        return value
    }

    override fun savePinCode(value: PinCode) {
        logDebug("savePinCode value: $value", TAG)
        saveObject(value, PROPERTY_KEY_PIN_CODE)
    }

    override fun readPinCode(): PinCode? {
        val value = readObject(PinCode::class.java, PROPERTY_KEY_PIN_CODE)
        logDebug("readPinCode value: $value", TAG)
        return value
    }

//    override fun saveCode(value: String) {
//        logDebug("saveCode value: $value", TAG)
//        preferences.edit().putString(CODE_KEY, value).apply()
//    }
//
//    override fun readCode(): String? {
//        val value = preferences.getString(CODE_KEY, null)
//        logDebug("readCode value: $value", TAG)
//        return value
//    }
//
//    override fun saveencryptedPin(value: String?) {
//        logDebug("saveencryptedPin value: $value", TAG)
//        preferences.edit().putString(ENCRYPTED_CODE_KEY, value).apply()
//    }
//
//    override fun readencryptedPin(): String? {
//        val value = preferences.getString(ENCRYPTED_CODE_KEY, null)
//        logDebug("readencryptedPin value: $value", TAG)
//        return value
//    }
//
//    override fun saveBiometricStatus(value: BiometricStatus) {
//        logDebug("saveBiometricStatus value: $value", TAG)
//        preferences.edit().putString(BIOMETRIC_STATUS_KEY, value.type).apply()
//    }
//
//    override fun readBiometricStatus(): BiometricStatus {
//        val value =
//            preferences.getString(BIOMETRIC_STATUS_KEY, BiometricStatus.UNSPECIFIED.type)?.let {
//                getEnumTypeValue<BiometricStatus>(it)
//            } ?: BiometricStatus.UNSPECIFIED
//        logDebug("readBiometricStatus value: $value", TAG)
//        return value
//    }

    override fun saveCurrentLanguage(value: AppLanguage) {
        logDebug("saveCurrentLanguage value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_CURRENT_LANGUAGE, value.type).apply()
    }

    override fun readCurrentLanguage(): AppLanguage {
        val value =
            preferences.getString(PROPERTY_KEY_CURRENT_LANGUAGE, AppLanguage.BG.type)?.let {
                getEnumTypeValue<AppLanguage>(it)
            } ?: AppLanguage.BG
        logDebug("readCurrentLanguage value: $value", TAG)
        return value
    }

    override fun saveUser(value: UserModel) {
        logDebug("saveUser value: $value", TAG)
        saveObject(value, PROPERTY_KEY_USER)
    }

    override fun readUser(): UserModel? {
        val value = readObject(UserModel::class.java, PROPERTY_KEY_USER)
        logDebug("readUser value: $value", TAG)
        return value
    }

    override fun saveAppStatus(value: AppStatus) {
        logDebug("saveAppStatus value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_APP_STATUS, value.type).apply()
    }

    override fun readAppStatus(): AppStatus {
        val value = preferences.getString(PROPERTY_KEY_APP_STATUS, AppStatus.NOT_READY.type)?.let {
            getEnumTypeValue<AppStatus>(it)
        } ?: AppStatus.NOT_READY
        logDebug("readAppStatus value: $value", TAG)
        return value
    }

    override fun saveEvrotrustTransactionIdForLogin(value: String) {
        logDebug("saveEvrotrustTransactionIdForLogin value: $value", TAG)
        preferences.edit().putString(PROPERTY_KEY_EVROTRUST_TRANSACTION_ID_FOR_LOGIN, value).apply()
    }

    override fun readEvrotrustTransactionIdForLogin(): String? {
        val value = preferences.getString(PROPERTY_KEY_EVROTRUST_TRANSACTION_ID_FOR_LOGIN, null)
        logDebug("readEvrotrustTransactionIdForLogin value: $value", TAG)
        return value
    }

    override fun logoutFromPreferences() {
        logDebug("logoutFromPreferences", TAG)
        preferences.edit {
            remove(PROPERTY_KEY_ACCESS_TOKEN)
            remove(PROPERTY_KEY_REFRESH_TOKEN)
            remove(PROPERTY_KEY_DATABASE)
            remove(PROPERTY_KEY_SERVER_PUBLIC)
            remove(PROPERTY_KEY_PIN_CODE)
            remove(PROPERTY_KEY_USER)
            putString(PROPERTY_KEY_APP_STATUS, AppStatus.NOT_READY.type)
        }
    }

    private fun saveObject(dataObject: Any, key: String) {
        val objectJson = Gson().toJson(dataObject)
        preferences.edit().putString(key, objectJson).apply()
    }

    private fun <T> readObject(baseClass: Class<T>, key: String): T? {
        val dataObject: String? = preferences.getString(key, "")
        return try {
            Gson().fromJson(dataObject, baseClass)
        } catch (e: Exception) {
            logError("readObject Exception: ${e.message}", e, TAG)
            null
        }
    }

}