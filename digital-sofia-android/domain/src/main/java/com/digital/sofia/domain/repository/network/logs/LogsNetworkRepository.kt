package com.digital.sofia.domain.repository.network.logs

import com.digital.sofia.domain.models.base.ResultEmittedData
import kotlinx.coroutines.flow.Flow
import java.io.File

interface LogsNetworkRepository {

    fun uploadLogs(
        personalIdentifier: String,
        files: List<File>,
    ): Flow<ResultEmittedData<Unit>>
}