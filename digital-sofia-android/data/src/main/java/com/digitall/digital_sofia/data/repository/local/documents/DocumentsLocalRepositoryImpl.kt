package com.digitall.digital_sofia.data.repository.local.documents

import android.os.Bundle
import com.digitall.digital_sofia.data.extensions.getSerializableCompat
import com.digitall.digital_sofia.data.repository.local.base.StateRepository
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.repository.local.documents.DocumentsLocalRepository

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsLocalRepositoryImpl : DocumentsLocalRepository, StateRepository {

    override var documents: List<DocumentModel>? = null

    override fun clear() {
        documents = null
    }

    override fun saveRepositoryState(bundle: Bundle) {
        bundle.putSerializable(javaClass.name, this)
    }

    override fun restoreRepositoryState(bundle: Bundle) {
        bundle.getSerializableCompat<DocumentsLocalRepository>(javaClass.name)?.let {
            documents = it.documents
        }
    }
}