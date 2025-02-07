package com.digital.sofia.domain.usecase.user

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.user.UserProfileStatusChangesRequestModel
import com.digital.sofia.domain.repository.network.user.UserRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

class SubscribeForUserStatusChangeUseCase(
    private val userRepository: UserRepository,
) {

    companion object {
        private const val TAG = "SubscribeForUserStatusChangeUseCaseTag"
    }

    fun invoke(identificationNumber: String?): Flow<ResultEmittedData<Unit>> {
        logDebug("subscribeForUserStatusChanges", TAG)
        return userRepository.subscribeForUserProfileStatusChanges(
            data = UserProfileStatusChangesRequestModel(
                identificationNumber = identificationNumber
            )
        )
    }
}