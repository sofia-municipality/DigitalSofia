/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents.list

import com.digital.sofia.models.documents.DocumentDownloadModel
import com.digital.sofia.models.documents.DocumentUi
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.digital.sofia.utils.DefaultDiffUtilCallback
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.paging.PagedListDelegationAdapter

class DocumentsAdapter(
    private val documentsDelegate: DocumentsDelegate,
    private val documentsHeaderDelegate: DocumentsHeaderDelegate,
) : PagedListDelegationAdapter<DocumentsAdapterMarker>(DefaultDiffUtilCallback()) {

    var clickListener: ClickListener? = null
        set(value) {
            field = value
            documentsDelegate.showClickListener = { clickListener?.onShowFileClicked(it) }
            documentsDelegate.downloadClickListener = { clickListener?.onDownloadFileClicked(it) }
        }

    init {
        delegatesManager.apply {
            addDelegate(documentsDelegate)
            addDelegate(documentsHeaderDelegate)
        }
    }

    interface ClickListener {
        fun onShowFileClicked(downloadModel: DocumentDownloadModel)
        fun onDownloadFileClicked(downloadModel: DocumentDownloadModel)
    }
}