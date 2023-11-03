package com.digitall.digital_sofia.domain.repository.network.documents

import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import kotlinx.coroutines.flow.Flow

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

fun interface DocumentsNetworkRepository {

    fun getDocuments(): Flow<ResultEmittedData<List<DocumentModel>>>

}