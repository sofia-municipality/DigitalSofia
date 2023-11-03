package com.digitall.digital_sofia.ui.fragments.main

import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.domain.extensions.readOnly
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCase
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class MainTabsFlowViewModel(
    private val documentsUseCase: DocumentsUseCase,
    logoutUseCase: LogoutUseCase,
    preferences: PreferencesRepository,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
) : BaseViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
) {

    companion object {
        private const val TAG = "MainTabsFlowViewModelTag"
    }

    override val needUpdateDocuments: Boolean = true

    private val _documentsForSignLiveData = MutableStateFlow(false)
    val documentsForSignLiveData = _documentsForSignLiveData.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        subscribeToDocuments()
    }

    private fun subscribeToDocuments() {
        logDebug("subscribeToDocuments", TAG)
        documentsUseCase.subscribeToUnsignedDocuments().onEach {
            if (it.isEmpty()) {
                logDebug("subscribeToDocuments isEmpty", TAG)
                _documentsForSignLiveData.value = false
            } else {
                logDebug("subscribeToDocuments isNotEmpty", TAG)
                _documentsForSignLiveData.value = true
            }
        }.launch(viewModelScope)
    }

}