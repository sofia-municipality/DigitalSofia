/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.signing.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.digital.sofia.databinding.ListItemWaitingDocumentBinding
import com.digital.sofia.extensions.inflateBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.models.forms.HomeAdapterMarker
import com.digital.sofia.models.forms.UnsignedDocumentUi
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

class SigningDelegate : AdapterDelegate<MutableList<HomeAdapterMarker>>() {

    var clickListener: ((evrotrustTransactionId: String) -> Unit)? = null

    override fun isForViewType(items: MutableList<HomeAdapterMarker>, position: Int): Boolean {
        return items[position] is UnsignedDocumentUi
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(parent.inflateBinding(ListItemWaitingDocumentBinding::inflate))
    }

    override fun onBindViewHolder(
        items: MutableList<HomeAdapterMarker>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        (holder as ViewHolder).bind(items[position] as UnsignedDocumentUi)
    }

    private inner class ViewHolder(
        private val binding: ListItemWaitingDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: UnsignedDocumentUi) {
            binding.tvTitle.text = model.title
            binding.root.onClickThrottle { clickListener?.invoke(model.evrotrustTransactionId) }
        }
    }

}