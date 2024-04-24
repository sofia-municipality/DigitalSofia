/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.network.base

import com.digital.sofia.data.models.network.base.BaseResponse
import com.digital.sofia.data.models.network.base.EmptyResponse
import com.digital.sofia.data.models.network.base.ErrorResponse
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException

abstract class BaseRepository(
    private val coroutineContextProvider: CoroutineContextProvider,
) : KoinComponent {

    companion object {
        private const val TAG = "BaseRepositoryTag"
    }

    abstract suspend fun refreshAccessToken()

    protected suspend fun <T : BaseResponse> getResult(
        call: suspend () -> retrofit2.Response<T>
    ): ResultEmittedData<T> {
        return try {
            val response = call()
            val isSuccessful = response.isSuccessful
            val responseBody = response.body() ?: EmptyResponse() as T
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
                isSuccessful && responseCode == 201 -> {
                    logDebug("isSuccessful && responseCode == 201", TAG)
                    dataSuccess(responseBody)
                }

                isSuccessful && responseCode == 204 -> {
                    logDebug("isSuccessful && responseCode == 204", TAG)
                    dataSuccess(responseBody)
                }

                isSuccessful && successCode && haveValidBody -> {
                    logDebug("isSuccessful && successCode && haveValidBody", TAG)
                    dataSuccess(responseBody)
                }

                isSuccessful && successCode && !haveValidBody -> {
                    logError("isSuccessful && successCode && !haveValidBody", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                isSuccessful && responseCode == 401 -> {
                    logError("isSuccessful && responseCode == 401", TAG)
                    refreshAccessToken()
                    dataRetry()
                }

                isSuccessful -> {
                    logError("isSuccessful", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && responseCode == 401 -> {
                    logError("!isSuccessful && responseCode == 401", TAG)
                    refreshAccessToken()
                    dataRetry()
                }

                !isSuccessful && responseCode == 443 -> {
                    logError("!isSuccessful && responseCode == 443", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && responseCode == 502 -> {
                    logError("!isSuccessful && responseCode == 502", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && responseCode == 500 -> {
                    logError("!isSuccessful && responseCode == 500", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                !isSuccessful && haveErrorBody -> {
                    logError("!isSuccessful && haveErrorBody", TAG)
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? = try {
                        Gson().fromJson(
                            response.errorBody()?.charStream()?.use { it.readText() },
                            type
                        )
                    } catch (e: java.lang.Exception) {
                        logError("ErrorResponse fromJson Exception: $e", TAG)
                        null
                    }
                    dataError(
                        serverType = errorResponse?.type ?: "Exception",
                        serverMessage = errorResponse?.message ?: (errorResponse?.errorDescription
                            ?: response.message()),
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

                else -> {
                    logError("else", TAG)
                    dataError(
                        serverType = serverType,
                        serverMessage = serverMessage,
                        responseCode = responseCode,
                        responseMessage = responseMessage,
                    )
                }

            }
        } catch (exception: Exception) {
            logError(
                "getResult Exception message: ${exception.message}\nstackTrace: ${exception.stackTrace}",
                TAG
            )
            when (exception) {
                is UnknownHostException -> {
                    logError("Exception is UnknownHostException", TAG)
                    dataError(
                        serverType = null,
                        serverMessage = null,
                        responseCode = 0,
                        responseMessage = exception.message ?: exception.toString(),
                    )
                }

                is SSLPeerUnverifiedException -> {
                    logError("Exception is SSLPeerUnverifiedException", TAG)
                    dataError(
                        serverType = null,
                        serverMessage = null,
                        responseCode = 0,
                        responseMessage = exception.message ?: exception.toString(),
                    )
                }

                else -> {
                    logError("Exception else", TAG)
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

    private fun <T> dataRetry(): ResultEmittedData<T> = ResultEmittedData.retry()

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
    ) = withContext(coroutineContextProvider.io) {
        asyncMethod()
    }


}