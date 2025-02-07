/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.pending.list

import com.digital.sofia.models.forms.PendingAdapterMarker
import com.digital.sofia.models.forms.PendingDocumentUi
import com.digital.sofia.utils.DefaultDiffUtilCallback
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.paging.PagedListDelegationAdapter

class PendingAdapter(
    private val pendingDelegate: PendingDelegate,
) : PagedListDelegationAdapter<PendingAdapterMarker>(DefaultDiffUtilCallback()) {

    var clickListener: ClickListener? = null
        set(value) {
            field = value
            pendingDelegate.clickListener = { clickListener?.onDocumentClicked(it) }
        }

    init {
        delegatesManager.apply {
            addDelegate(pendingDelegate)
        }
    }

    fun interface ClickListener {
        fun onDocumentClicked(document: PendingDocumentUi)
    }
}