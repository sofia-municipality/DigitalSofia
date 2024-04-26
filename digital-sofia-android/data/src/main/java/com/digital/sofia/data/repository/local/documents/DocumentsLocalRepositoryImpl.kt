/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.local.documents

import android.os.Bundle
import com.digital.sofia.data.extensions.getSerializableCompat
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.repository.local.documents.DocumentsLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DocumentsLocalRepositoryImpl : DocumentsLocalRepository {

    companion object {
        private const val TAG = "DocumentsLocalRepositoryTag"
        private const val DOCUMENTS_LOCAL_REPOSITORY_KEY = "DOCUMENTS_LOCAL_REPOSITORY_KEY"
    }

    private val dataList: MutableList<DocumentModel> = mutableListOf()

    private val dataFlow = MutableStateFlow<List<DocumentModel>>(emptyList())

    override fun getAll(): List<DocumentModel> {
        return dataList
    }

    override fun addAll(data: List<DocumentModel>) {
        dataList.addAll(data)
        dataFlow.value = dataList
    }

    override fun replaceAll(data: List<DocumentModel>) {
        dataList.clear()
        dataList.addAll(data)
        dataFlow.value = dataList
    }

    override fun subscribeToAll(): Flow<List<DocumentModel>> {
       return dataFlow
    }

    override fun saveState(bundle: Bundle) {
        bundle.putSerializable(DOCUMENTS_LOCAL_REPOSITORY_KEY, ArrayList(dataList))
    }

    override fun restoreState(bundle: Bundle) {
        (bundle.getSerializableCompat(DOCUMENTS_LOCAL_REPOSITORY_KEY) as? ArrayList<DocumentModel>)?.let {
            dataList.addAll(it)
            dataFlow.value = dataList
        }
    }

    override fun clear() {
        dataList.clear()
        dataFlow.value = emptyList()
    }
}