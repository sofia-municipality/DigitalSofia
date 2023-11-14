package com.digitall.digital_sofia.ui.fragments.permissions

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.mappers.common.PermissionNamePmMapper
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.ui.fragments.permissions.PermissionBottomSheetFragment.Companion.IS_PERMISSION_GRANTED_BUNDLE_KEY
import com.digitall.digital_sofia.ui.fragments.permissions.PermissionBottomSheetFragment.Companion.PERMISSION_REQUEST_KEY
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.PermissionsManager.Companion.isPermissionGranted
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class PermissionBottomSheetViewModel(
    private val permissionNamePmMapper: PermissionNamePmMapper,
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
        private const val TAG = "PermissionBottomSheetViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

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
