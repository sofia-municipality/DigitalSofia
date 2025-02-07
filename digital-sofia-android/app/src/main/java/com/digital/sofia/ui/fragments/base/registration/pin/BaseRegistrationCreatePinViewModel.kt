/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.registration.pin

import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digital.sofia.ui.fragments.base.pin.create.BaseCreatePinViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SupportBiometricManager

abstract class BaseRegistrationCreatePinViewModel(
    loginTimer: LoginTimer,
    preferences: PreferencesRepository,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    biometricManager: SupportBiometricManager,
    cryptographyRepository: CryptographyRepository,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
    createCodeResponseErrorToStringMapper: CreateCodeResponseErrorToStringMapper,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
) : BaseCreatePinViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    biometricManager = biometricManager,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    cryptographyRepository = cryptographyRepository,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
    createCodeResponseErrorToStringMapper = createCodeResponseErrorToStringMapper,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
) {

    final override val isAuthorizationActive: Boolean = false

}