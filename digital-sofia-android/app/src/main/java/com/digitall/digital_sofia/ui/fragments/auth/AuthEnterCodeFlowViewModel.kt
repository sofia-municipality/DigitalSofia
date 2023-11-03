package com.digitall.digital_sofia.ui.fragments.auth

import android.content.Context
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.common.BiometricStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.models.common.StartDestination
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class AuthEnterCodeFlowViewModel(
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
        private const val TAG = "AuthEnterFlowViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    fun getStartDestination(context: Context): StartDestination {
        val isBiometricAvailable = SupportBiometricManager.hasBiometrics(context) &&
                preferences.readPinCode()?.biometricStatus == BiometricStatus.BIOMETRIC
        return if (isBiometricAvailable) {
            logDebug("to enterBiometricFragment", TAG)
            StartDestination(R.id.enterBiometricFragment)
        } else {
            logDebug("to enterPinFragment", TAG)
            StartDestination(R.id.enterPinFragment)
        }
    }

}