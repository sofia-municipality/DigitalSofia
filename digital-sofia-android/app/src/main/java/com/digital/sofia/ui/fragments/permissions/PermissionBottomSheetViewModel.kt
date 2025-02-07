/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.permissions

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.mappers.common.PermissionNamePmMapper
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.ui.fragments.permissions.PermissionBottomSheetFragment.Companion.IS_PERMISSION_GRANTED_BUNDLE_KEY
import com.digital.sofia.ui.fragments.permissions.PermissionBottomSheetFragment.Companion.PERMISSION_REQUEST_KEY
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.PermissionsManager.Companion.isPermissionGranted

class PermissionBottomSheetViewModel(
    private val permissionNamePmMapper: PermissionNamePmMapper,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    preferences: PreferencesRepository,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    cryptographyRepository: CryptographyRepository,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : BaseViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "PermissionBottomSheetViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    private lateinit var permissionId: String

    fun setupPermissionFromArgs(permission: String) {
        logDebug("setupPermissionFromArgs permission: $permission", TAG)
        this.permissionId = permission
    }

    fun getPermissionName(): String {
        logDebug("getPermissionName", TAG)
        return permissionNamePmMapper.map(permissionId)
    }

    fun dismissIfPermissionGranted(fragment: PermissionBottomSheetFragment) {
        logDebug("dismissIfPermissionGranted", TAG)
        if (isPermissionGranted(fragment.requireContext(), permissionId)) {
            fragment.setFragmentResult(
                PERMISSION_REQUEST_KEY,
                bundleOf(IS_PERMISSION_GRANTED_BUNDLE_KEY to true)
            )
            fragment.dismiss()
        }
    }

    fun handleDismissAction(fragment: PermissionBottomSheetFragment) {
        logDebug("handleDismissAction", TAG)
        if (isPermissionGranted(fragment.requireContext(), permissionId).not()) {
            fragment.setFragmentResult(
                PERMISSION_REQUEST_KEY,
                bundleOf(IS_PERMISSION_GRANTED_BUNDLE_KEY to false)
            )
        }
    }

}
