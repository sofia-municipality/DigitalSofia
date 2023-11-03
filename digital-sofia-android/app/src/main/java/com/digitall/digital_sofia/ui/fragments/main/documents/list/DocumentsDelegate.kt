package com.digitall.digital_sofia.ui.fragments.main.documents.list

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.ListItemDocumentBinding
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel
import com.digitall.digital_sofia.extensions.inflateBinding
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.extensions.setBackgroundColorResource
import com.digitall.digital_sofia.extensions.setTextResource
import com.digitall.digital_sofia.models.documents.DocumentUi
import com.digitall.digital_sofia.models.documents.DocumentsAdapterMarker
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsDelegate : AdapterDelegate<MutableList<DocumentsAdapterMarker>>() {

    var clickListener: ((evrotrustTransactionId: String) -> Unit)? = null
    var showClickListener: ((url: String) -> Unit)? = null
    var downloadClickListener: ((url: String) -> Unit)? = null

    override fun isForViewType(items: MutableList<DocumentsAdapterMarker>, position: Int): Boolean {
        return items[position] is DocumentUi
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(parent.inflateBinding(ListItemDocumentBinding::inflate))
    }

    override fun onBindViewHolder(
        items: MutableList<DocumentsAdapterMarker>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        (holder as ViewHolder).bind(items[position] as DocumentUi)
    }

    private inner class ViewHolder(
        private val binding: ListItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: DocumentUi) {
            binding.tvTitle.text = "Столична Община"
            binding.tvNumber.text = model.evrotrustTransactionId
            binding.tvFileName.text = model.fileName
            binding.tvCreatedOnValue.text = model.created
            binding.tvSignedOnValue.text = model.signed
            binding.tvShowFile.isVisible = model.fileUrl.isNotEmpty()
            binding.tvDownloadFile.isVisible = model.fileUrl.isNotEmpty()
            val btnStatusColor = when (model.status) {
                DocumentStatusModel.SIGNED -> R.color.color_green
                DocumentStatusModel.SIGNING -> R.color.color_light_blue
                DocumentStatusModel.UNKNOWN,
                DocumentStatusModel.EXPIRED -> R.color.color_red
                else -> R.color.color_green
            }
            binding.btnStatus.setBackgroundColorResource(btnStatusColor)
            val btnTextRes = when (model.status) {
                DocumentStatusModel.SIGNED -> R.string.document_status_signed
                DocumentStatusModel.SIGNING -> R.string.document_status_wait_sign
                DocumentStatusModel.EXPIRED -> R.string.document_status_expired
                DocumentStatusModel.REJECTED -> R.string.document_status_rejected
                DocumentStatusModel.PENDING -> R.string.document_status_pending
                DocumentStatusModel.UNSIGNED -> R.string.document_status_unsigned
                DocumentStatusModel.UNKNOWN -> R.string.document_status_unknown

            }
            binding.btnStatus.setTextResource(btnTextRes)
            binding.root.onClickThrottle { clickListener?.invoke(model.evrotrustTransactionId) }
            binding.tvShowFile.onClickThrottle { showClickListener?.invoke(model.fileUrl) }
            binding.tvDownloadFile.onClickThrottle { downloadClickListener?.invoke(model.fileUrl) }
        }
    }

}