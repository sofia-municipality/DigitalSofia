/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.documents

data class DocumentsWithPaginationModel(
    val documents: List<DocumentModel>,
    val cursor: String?,
)