package com.digitall.digital_sofia.ui.fragments.error.blocked.blocked

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.navigateNewRootInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class BlockedViewModel(
    private val currentContext: CurrentContext,
    private val preferences: PreferencesRepository,
    logoutUseCase: LogoutUseCase,
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
        private const val TAG = "BlockedViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    private val _blockedText = MutableLiveData<String>()
    val blockedText = _blockedText.readOnly()

    override fun onFirstAttach() {
        logDebug("onFirstAttach", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode?.errorTimeCode == null) {
            logout()
            return
        }
        val timeOnUnlock = pinCode.errorTimeCode!! + pinCode.errorStatus.timeoutMillis
        logDebug("timeOnUnlock: $timeOnUnlock", TAG)
        logDebug("currentTimeMillis: ${System.currentTimeMillis()}", TAG)
        if (System.currentTimeMillis() < timeOnUnlock) {
            logDebug("System.currentTimeMillis() < timeOnUnlock", TAG)
            viewModelScope.launch {
                while (System.currentTimeMillis() < timeOnUnlock) {
                    val timeLeft = (timeOnUnlock - System.currentTimeMillis()) / 1000L
                    _blockedText.value = currentContext.get().getString(
                        R.string.error_blocked_description,
                        timeLeft.toString()
                    )
                    delay(1000)
                }
                findActivityNavController().navigateNewRootInMainThread(
                    NavActivityDirections.toEnterCodeFlowFragment(), viewModelScope
                )
            }
        } else {
            logDebug("System.currentTimeMillis() > timeOnUnlock", TAG)
            findActivityNavController().navigateNewRootInMainThread(
                NavActivityDirections.toEnterCodeFlowFragment(), viewModelScope
            )
        }
    }
}