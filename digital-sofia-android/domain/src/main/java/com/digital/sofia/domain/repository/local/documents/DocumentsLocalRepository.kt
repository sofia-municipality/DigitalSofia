/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.local.documents

import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.repository.local.base.BaseLocalRepository
import com.digital.sofia.domain.repository.local.base.BaseLocalRepositoryWithDataMethods

interface DocumentsLocalRepository : BaseLocalRepositoryWithDataMethods<List<DocumentModel>>