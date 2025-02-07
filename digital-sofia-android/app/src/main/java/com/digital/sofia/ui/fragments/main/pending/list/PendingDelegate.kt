/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.pending.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.digital.sofia.databinding.ListItemWaitingDocumentBinding
import com.digital.sofia.extensions.inflateBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.models.forms.PendingAdapterMarker
import com.digital.sofia.models.forms.PendingDocumentUi
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

class PendingDelegate : AdapterDelegate<MutableList<PendingAdapterMarker>>() {

    var clickListener: ((document: PendingDocumentUi) -> Unit)? = null

    override fun isForViewType(items: MutableList<PendingAdapterMarker>, position: Int): Boolean {
        return items[position] is PendingDocumentUi
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(parent.inflateBinding(ListItemWaitingDocumentBinding::inflate))
    }

    override fun onBindViewHolder(
        items: MutableList<PendingAdapterMarker>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        (holder as ViewHolder).bind(items[position] as PendingDocumentUi)
    }

    private inner class ViewHolder(
        private val binding: ListItemWaitingDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: PendingDocumentUi) {
            binding.tvTitle.text = model.title
            binding.root.onClickThrottle { clickListener?.invoke(model) }
        }
    }

}