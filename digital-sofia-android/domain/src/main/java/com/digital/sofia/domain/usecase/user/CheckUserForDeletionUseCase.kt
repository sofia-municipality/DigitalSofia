package com.digital.sofia.domain.usecase.user

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.repository.network.user.UserRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

class CheckUserForDeletionUseCase(
    private val userRepository: UserRepository,
) {

    companion object {
        private const val TAG = "CheckUserForDeletionUseCaseTag"
    }

    fun invoke(): Flow<ResultEmittedData<Unit>> {
        logDebug("checkUserForDeletion", TAG)
        return userRepository.checkUserForDeletion()
    }
}