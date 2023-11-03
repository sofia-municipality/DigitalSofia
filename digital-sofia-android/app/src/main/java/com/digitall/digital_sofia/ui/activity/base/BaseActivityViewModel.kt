package com.digitall.digital_sofia.ui.activity.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.BuildConfig
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.models.common.StartDestination
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.ActivitiesCommonHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.ScreenshotsDetector
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

/**
 * Use single activity
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

abstract class BaseActivityViewModel(
    private val rootBeer: RootBeer,
    private val dispatcherIO: CoroutineDispatcher,
    private val screenshotsDetector: ScreenshotsDetector,
    private val activitiesCommonHelper: ActivitiesCommonHelper,
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
        private const val TAG = "BaseActivityViewModelTag"
    }

    abstract fun getStartDestination(intent: Intent): StartDestination

    abstract fun onSaveInstanceState(bundle: Bundle)

    abstract fun onRestoreInstanceState(bundle: Bundle)

    abstract fun onPasswordRequiredAuthError()

    abstract fun onProfileBlockedError()

    abstract fun onForceUpdateError()

    abstract fun onRootDetectedError()

    abstract fun logout(finishCallback: (() -> Unit)? = null)

    @CallSuper
    override fun onFirstAttach() {
        super.onFirstAttach()
        logDebug("onFirstAttach", TAG)
        activitiesCommonHelper.getFcmToken()
        checkInternalRoot()
    }

    fun applyLightDarkTheme() {
        activitiesCommonHelper.applyLightDarkTheme()
    }

    fun onResume(activity: Activity) {
        screenshotsDetector.startDetecting(activity) {
            // TODO
        }
    }

    fun onPause(activity: Activity) {
        // TODO should be removed in prod
        screenshotsDetector.stopDetecting(activity)
    }

    override fun onBackPressed() {
        if (!findActivityNavController().popBackStack()) {
            closeActivity()
        }
    }

    private fun checkInternalRoot() {
        if (!BuildConfig.DEBUG) {
            viewModelScope.launch(dispatcherIO) {
                if (rootBeer.isRooted) {
                    logout(::onRootDetectedError)
                }
            }
        }
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
    }
}