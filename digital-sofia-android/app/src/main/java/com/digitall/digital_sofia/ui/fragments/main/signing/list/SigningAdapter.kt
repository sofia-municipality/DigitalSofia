package com.digitall.digital_sofia.ui.fragments.main.signing.list

import com.digitall.digital_sofia.models.forms.HomeAdapterMarker
import com.digitall.digital_sofia.utils.DefaultDiffUtilCallback
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class SigningAdapter(
    private val signingDelegate: SigningDelegate,
) : AsyncListDifferDelegationAdapter<HomeAdapterMarker>(DefaultDiffUtilCallback()) {

    var clickListener: ClickListener? = null
        set(value) {
            field = value
            signingDelegate.clickListener = { clickListener?.onDocumentClicked(it) }

        }

    init {
        items = mutableListOf()
        delegatesManager.apply {
            addDelegate(signingDelegate)
        }
    }

    fun interface ClickListener {
        fun onDocumentClicked(evrotrustTransactionId: String)
    }
}