package com.digital.sofia.domain.repository.network.user

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.user.UserProfileStatusChangesRequestModel
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun deleteUser(): Flow<ResultEmittedData<Unit>>

    fun checkUserForDeletion(): Flow<ResultEmittedData<Unit>>

    fun subscribeForUserProfileStatusChanges(data: UserProfileStatusChangesRequestModel): Flow<ResultEmittedData<Unit>>

}