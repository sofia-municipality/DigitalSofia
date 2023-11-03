package com.digitall.digital_sofia.domain.usecase.logout

import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

fun interface LogoutUseCase {

    fun logout(): Flow<ResultEmittedData<Unit>>

}

class LogoutUseCaseImpl(
    private val preferences: PreferencesRepository,
    private val documentsDatabaseRepository: DocumentsDatabaseRepository,
) : LogoutUseCase {

    override fun logout(): Flow<ResultEmittedData<Unit>> {
        return flow {
            emit(ResultEmittedData.loading(null))
            preferences.logoutFromPreferences()
            documentsDatabaseRepository.clear()
            emit(ResultEmittedData.success(Unit))
        }
    }

}