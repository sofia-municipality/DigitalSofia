package com.digitall.digital_sofia.ui.activity.main

import android.content.Intent
import android.os.Bundle
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.models.common.StartDestination
import com.digitall.digital_sofia.ui.activity.base.BaseActivityViewModel
import com.digitall.digital_sofia.utils.ActivitiesCommonHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.ScreenshotsDetector
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Use single activity
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

class MainViewModel(
    rootBeer: RootBeer,
    logoutUseCase: LogoutUseCase,
    preferences: PreferencesRepository,
    dispatcherIO: CoroutineDispatcher,
    screenshotsDetector: ScreenshotsDetector,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
    activitiesCommonHelper: ActivitiesCommonHelper,
) : BaseActivityViewModel(
    rootBeer = rootBeer,
    preferences = preferences,
    dispatcherIO = dispatcherIO,
    logoutUseCase = logoutUseCase,
    screenshotsDetector = screenshotsDetector,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    activitiesCommonHelper = activitiesCommonHelper,
    cryptographyRepository = cryptographyRepository,
) {

    override val needUpdateDocuments: Boolean = false

    override fun getStartDestination(intent: Intent): StartDestination {
        return StartDestination(R.id.startFlowFragment)
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        // TODO
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        // TODO
    }

    override fun onPasswordRequiredAuthError() {
        // TODO
    }

    override fun onProfileBlockedError() {
        // TODO
    }

    override fun onForceUpdateError() {
        // TODO
    }

    override fun onRootDetectedError() {
        // TODO
    }

    override fun logout(finishCallback: (() -> Unit)?) {
        // TODO
    }

}