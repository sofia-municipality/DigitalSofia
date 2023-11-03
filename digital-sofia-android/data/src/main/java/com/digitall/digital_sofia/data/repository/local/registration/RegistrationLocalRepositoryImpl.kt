package com.digitall.digital_sofia.data.repository.local.registration

import android.os.Bundle
import com.digitall.digital_sofia.data.extensions.getSerializableCompat
import com.digitall.digital_sofia.data.repository.local.base.StateRepository
import com.digitall.digital_sofia.domain.repository.local.registration.RegistrationLocalRepository

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationLocalRepositoryImpl : RegistrationLocalRepository, StateRepository {

    override fun clear() {
        // no data
    }

    override fun saveRepositoryState(bundle: Bundle) {
        bundle.putSerializable(javaClass.name, this)
    }

    override fun restoreRepositoryState(bundle: Bundle) {
        bundle.getSerializableCompat<RegistrationLocalRepository>(javaClass.name)?.let {

        }
    }
}