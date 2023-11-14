package com.digitall.digital_sofia.domain.repository.local.documents

import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface DocumentsLocalRepository : Serializable {

    var documents: List<DocumentModel>?

    fun clear()
}