package com.digitall.digital_sofia.data.repository.network.base

import com.digitall.digital_sofia.data.CLIENT_ID
import com.digitall.digital_sofia.data.CLIENT_SCOPE
import com.digitall.digital_sofia.data.CLIENT_SECRET
import com.digitall.digital_sofia.data.GRANT_TYPE
import com.digitall.digital_sofia.data.URL_AUTH
import com.digitall.digital_sofia.data.URL_AUTHORIZATION_GET_ACCESS_TOKEN_WITH_REFRESH_TOKEN
import com.digitall.digital_sofia.data.models.network.base.BaseResponse
import com.digitall.digital_sofia.data.models.network.base.ErrorResponse
import com.digitall.digital_sofia.data.utils.CoroutineContextProvider
import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseRepository : KoinComponent {

    companion object {
        private const val TAG = "BaseRepositoryTag"
    }

    private val contextProvider: CoroutineContextProvider by inject()
    private val client: OkHttpClient by inject()
    private val preferences: PreferencesRepository by inject()

    protected suspend fun <T : BaseResponse> getResult(call: suspend () -> retrofit2.Response<T>): ResultEmittedData<T> {
        return try {
            val response = call()
            val isSuccessful = response.isSuccessful
            val responseBody = response.body()
            val errorBody = response.errorBody()
            val responseCode = response.code()
            val successCode = when (responseCode) {
                200,
                201,
                202,
                203,
                204,
                205,
                206,
                207,
                208,
                226 -> true

                else -> false
            }
            val responseMessage = response.message()
            val serverType = responseBody?.type
            val serverMessage = responseBody?.message
            val haveData = serverType.isNullOrEmpty() && serverMessage.isNullOrEmpty()
            val haveBody = responseBody != null
            val haveValidBody = haveBody && haveData
            val haveErrorBody = errorBody != null
            logDebug(
                "isSuccessful: $isSuccessful\n" +
                        "responseCode: $responseCode\n" +
                        "responseBody: $responseBody\n" +
                        "haveValidBody: $haveValidBody\n" +
                        "errorBody: $errorBody\n" +
                        "serverType: $serverType\n" +
                        "serverMessage: $serverMessage\n" +
                        "responseMessage: $responseMessage", TAG
            )
            when {
                isSuccessful && successCode && haveValidBody -> {
                    logDebug("Case 1", TAG)
                    dataSuccess(responseBody!!)
                }

                isSuccessful && successCode && !haveValidBody -> {
                    logError("Case 2", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                isSuccessful && responseCode == 401 -> {
                    logError("Case 3", TAG)
                    startAuthorization()
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                isSuccessful -> {
                    logError("Case 4", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && responseCode == 401 -> {
                    logError("Case 5", TAG)
                    startAuthorization()
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && responseCode == 443 -> {
                    logError("Case 16", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && responseCode == 502 -> {
                    logError("Case 6", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && responseCode == 500 -> {
                    logError("Case 7", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && haveErrorBody -> {
                    logError("Case 8", TAG)
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? = try {
                        Gson().fromJson(response.errorBody()?.charStream(), type)
                    } catch (e: java.lang.Exception) {
                        logError("ErrorResponse fromJson Exception: $e", TAG)
                        null
                    }
                    dataError(
                        serverType = errorResponse?.type ?: "Exception",
                        serverMessage = errorResponse?.message ?: response.message(),
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                else -> {
                    logError("Case 9", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

            }
        } catch (exception: Exception) {
            logError("getResult Exception: ${exception.message}", TAG)
            when (exception) {
                is UnknownHostException -> {
                    logError("Case 10", TAG)
                    dataError(
                        serverType = null,
                        serverMessage = null,
                        responseCode = 0,
                        responseMessage = exception.message ?: exception.toString(),
                    )
                }

                is SSLPeerUnverifiedException -> {
                    logError("Case 11", TAG)
                    dataError(
                        serverType = null,
                        serverMessage = null,
                        responseCode = 0,
                        responseMessage = exception.message ?: exception.toString(),
                    )
                }

                else -> {
                    logError("Case 12", TAG)
                    dataError(
                        serverType = null,
                        serverMessage = null,
                        responseCode = 0,
                        responseMessage = exception.message ?: exception.toString(),
                    )
                }
            }

        }
    }

    private fun <T> dataError(
        responseCode: Int,
        serverType: String?,
        serverMessage: String?,
        responseMessage: String?,
    ): ResultEmittedData<T> = ResultEmittedData.error(
        error = ResultEmittedData.Error(
            serverType = serverType,
            serverMessage = serverMessage,
            responseCode = responseCode,
            responseMessage = responseMessage,
        )
    )

    private fun <T> dataSuccess(
        data: T,
    ): ResultEmittedData<T> = ResultEmittedData.success(
        data = data
    )

    /**
     * Use this when you want do something async
     */
    protected suspend fun doAsync(
        asyncMethod: suspend () -> Unit
    ) = withContext(contextProvider.io) {
        asyncMethod()
    }

    private fun startAuthorization() {
        logDebug("startAuthorization()", TAG)
        val refreshTokenResult = refreshToken()
        if (refreshTokenResult) {
            logDebug("startAuthorization() refreshTokenResult.isSuccessful", TAG)
            return
        }
        logError("startAuthorization() refreshTokenResult.isNotSuccessful", TAG)
        val enterToAccountResult = enterToAccount()
        if (enterToAccountResult) {
            logDebug("startAuthorization() enterToAccountResult.isSuccessful", TAG)
            return
        }
        logError("startAuthorization() enterToAccountResult.isNotSuccessful", TAG)
    }

    private fun refreshToken(): Boolean {
        logDebug("refreshToken()", TAG)
        val refreshToken = preferences.readRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            logError("refreshToken() refreshToken.isNullOrEmpty()", TAG)
            return false
        }
        try {
            val requestBody: RequestBody = FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("scope", CLIENT_SCOPE)
                .add("grant_type", GRANT_TYPE)
                .add("refresh_token", refreshToken)
                .build()
            val request = okhttp3.Request.Builder()
                .url(URL_AUTH + URL_AUTHORIZATION_GET_ACCESS_TOKEN_WITH_REFRESH_TOKEN)
                .post(requestBody)
                .build()
            val response = client.newCall(request)
                .execute()
            val responseBody = response.body
            val responseCode = response.code
            val successCode = when (responseCode) {
                200,
                201,
                202,
                203,
                204,
                205,
                206,
                207,
                208,
                226 -> true

                else -> false
            }
            if (!response.isSuccessful || !successCode || responseBody == null) {
                logError(
                    "refreshToken() not isSuccessful, response.isSuccessful: ${response.isSuccessful}, successCode: $successCode",
                    TAG
                )
                return false
            }
            val bodyString = responseBody.string()
            if (bodyString.isEmpty()) {
                logError("refreshToken() bodyString.isEmpty()", TAG)
                return false
            }
            val objectBody = JSONObject(bodyString)
            val accessToken = objectBody.getString("access_token")
            val serverMessage = objectBody.getString("message")
            if (accessToken.isNullOrEmpty()) {
                logError("refreshToken() accessToken.isNullOrEmpty()", TAG)
                return false
            }
            preferences.saveAccessToken(accessToken)
            logDebug("enterToAccount() success", TAG)
            return true
        } catch (e: Exception) {
            logError("refreshToken() Exception: ${e.message}", e, TAG)
            return false
        }
    }

    private fun enterToAccount(): Boolean {
        logDebug("enterToAccount()", TAG)
        try {
            val user = preferences.readUser()
            if (user == null) {
                logError("enterToAccount() user == null", TAG)
                return false
            }
            val ern = user.personalIdentificationNumber
            if (ern.isNullOrEmpty()) {
                logError("enterToAccount() ern.isNullOrEmpty", TAG)
                return false
            }
            val hashedPin = preferences.readPinCode()?.hashedPin
            if (hashedPin.isNullOrEmpty()) {
                logError("enterToAccount() hashedPin.isNullOrEmpty", TAG)
                return false
            }
            val requestBody: RequestBody = FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("grant_type", GRANT_TYPE)
                .add("client_secret", CLIENT_SECRET)
                .add("scope", CLIENT_SCOPE)
                .add("pin", hashedPin)
                .add("egn", ern)
                .build()
            val request = okhttp3.Request.Builder()
                .url(URL_AUTH + URL_AUTHORIZATION_GET_ACCESS_TOKEN_WITH_REFRESH_TOKEN)
                .post(requestBody)
                .build()
            val response = client.newCall(request)
                .execute()
            val responseBody = response.body
            val responseCode = response.code
            val successCode = when (responseCode) {
                200,
                201,
                202,
                203,
                204,
                205,
                206,
                207,
                208,
                226 -> true

                else -> false
            }
            if (!response.isSuccessful || !successCode || responseBody == null) {
                logError("enterToAccount() not isSuccessful", TAG)
                return false
            }
            val bodyString = responseBody.string()
            if (bodyString.isEmpty()) {
                logError("enterToAccount() bodyString isEmpty", TAG)
                return false
            }
            val objectBody = JSONObject(bodyString)
            val accessToken = objectBody.getString("access_token")
            if (accessToken.isNullOrEmpty()) {
                logError("enterToAccount() accessToken isNullOrEmpty", TAG)
                return false
            }
            preferences.saveAccessToken(accessToken)
            val refreshToken = objectBody.getString("refresh_token")
            if (refreshToken.isNullOrEmpty()) {
                logError("enterToAccount() refreshToken isNullOrEmpty", TAG)
                return false
            }
            preferences.saveRefreshToken(refreshToken)
            logDebug("enterToAccount() success", TAG)
            return true
        } catch (e: Exception) {
            logError("enterToAccount() Exception: ${e.message}", e, TAG)
            return false
        }
    }
}