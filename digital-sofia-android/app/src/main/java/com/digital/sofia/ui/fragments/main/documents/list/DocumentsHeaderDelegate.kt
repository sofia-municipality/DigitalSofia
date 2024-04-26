package com.digital.sofia.ui.fragments.main.documents.list

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.digital.sofia.databinding.ListItemDocumentHeaderBinding
import com.digital.sofia.extensions.inflateBinding
import com.digital.sofia.models.documents.DocumentHeaderUi
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

class DocumentsHeaderDelegate: AdapterDelegate<MutableList<DocumentsAdapterMarker>>() {

    override fun isForViewType(items: MutableList<DocumentsAdapterMarker>, position: Int): Boolean {
        return items[position] is DocumentHeaderUi
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(parent.inflateBinding(ListItemDocumentHeaderBinding::inflate))
    }

    override fun onBindViewHolder(items: MutableList<DocumentsAdapterMarker>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val documentHeader = items[position] as DocumentHeaderUi
        (holder as ViewHolder).bind(
            iconRes = documentHeader.iconRes,
            titleRes = documentHeader.titleRes
        )
    }

    private inner class ViewHolder(
            private val binding: ListItemDocumentHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            @DrawableRes iconRes: Int,
            @StringRes titleRes: Int
        ) {
            binding.ivDocumentsIcon.setImageResource(iconRes)
            binding.tvTitle.setText(titleRes)
        }
    }

}