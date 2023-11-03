package com.digitall.digital_sofia.ui.fragments.main.documents.list

import com.digitall.digital_sofia.models.documents.DocumentsAdapterMarker
import com.digitall.digital_sofia.utils.DefaultDiffUtilCallback
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsAdapter(
    private val documentsDelegate: DocumentsDelegate,
) : AsyncListDifferDelegationAdapter<DocumentsAdapterMarker>(DefaultDiffUtilCallback()) {

    var clickListener: ClickListener? = null
        set(value) {
            field = value
            documentsDelegate.clickListener = { clickListener?.onDocumentClicked(it) }
            documentsDelegate.showClickListener = { clickListener?.onShowFileClicked(it) }
            documentsDelegate.downloadClickListener = { clickListener?.onDownloadFileClicked(it) }
        }

    init {
        items = mutableListOf()
        delegatesManager.apply {
            addDelegate(documentsDelegate)
        }
    }

    interface ClickListener {
        fun onShowFileClicked(url: String)
        fun onDownloadFileClicked(url: String)
        fun onDocumentClicked(evrotrustTransactionId: String)
    }
}