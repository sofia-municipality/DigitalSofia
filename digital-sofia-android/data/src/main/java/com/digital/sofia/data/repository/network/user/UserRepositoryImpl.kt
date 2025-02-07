package com.digital.sofia.data.repository.network.user

import com.digital.sofia.data.mappers.network.user.request.UserProfileStatusChangesRequestBodyMapper
import com.digital.sofia.data.network.user.UserApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.user.UserProfileStatusChangesRequestModel
import com.digital.sofia.domain.repository.network.user.UserRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UserRepositoryImpl(
    private val userApi: UserApi,
    private val userProfileStatusChangesRequestBodyMapper: UserProfileStatusChangesRequestBodyMapper,
    private val authorizationHelper: AuthorizationHelper,
    coroutineContextProvider: CoroutineContextProvider,
) : UserRepository, BaseRepository(
    coroutineContextProvider = coroutineContextProvider,
) {

    companion object {
        private const val TAG = "UserRepositoryTag"
    }

    override fun deleteUser(): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("deleteUser", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            userApi.deleteUser()
        }.onSuccess {
            logDebug("deleteUser onSuccess", TAG)
            emit(ResultEmittedData.success(Unit))
        }.onRetry {
            logDebug("deleteUser onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("deleteUser onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)


    override fun checkUserForDeletion(): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("checkIfUserCanBeDeleted", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            userApi.checkUserForDeletion()
        }.onSuccess {
            logDebug("checkIfUserCanBeDeleted onSuccess", TAG)
            emit(ResultEmittedData.success(Unit))
        }.onRetry {
            logDebug("checkIfUserCanBeDeleted onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("checkIfUserCanBeDeleted onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)


    override fun subscribeForUserProfileStatusChanges(data: UserProfileStatusChangesRequestModel): Flow<ResultEmittedData<Unit>> =
        flow {
            logDebug("subscribeForUserProfileStatusChanges", TAG)
            emit(ResultEmittedData.loading(null))
            getResult {
                userApi.subscribeForUserProfileStatusChanges(
                    requestBody = userProfileStatusChangesRequestBodyMapper.map(data)
                )
            }.onSuccess {
                logDebug("subscribeForUserProfileStatusChanges onSuccess", TAG)
                emit(ResultEmittedData.success(Unit))
            }.onRetry {
                logDebug("subscribeForUserProfileStatusChanges onRetry", TAG)
                emit(ResultEmittedData.retry(null))
            }.onFailure {
                logError("subscribeForUserProfileStatusChanges onFailure", it, TAG)
                emit(ResultEmittedData.error(it, null))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun refreshAccessToken() {
        authorizationHelper.startAuthorization()
    }
}