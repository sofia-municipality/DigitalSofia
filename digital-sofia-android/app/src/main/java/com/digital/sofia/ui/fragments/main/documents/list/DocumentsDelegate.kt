/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents.list

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.digital.sofia.R
import com.digital.sofia.databinding.ListItemDocumentBinding
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.extensions.convertDate
import com.digital.sofia.extensions.inflateBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.extensions.setBackgroundDrawableResource
import com.digital.sofia.extensions.setCompoundDrawablesExt
import com.digital.sofia.extensions.setTextResource
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.models.documents.DocumentDownloadModel
import com.digital.sofia.models.documents.DocumentUi
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

class DocumentsDelegate : AdapterDelegate<MutableList<DocumentsAdapterMarker>>() {

    var showClickListener: ((downloadModel: DocumentDownloadModel) -> Unit)? = null
    var downloadClickListener: ((downloadModel: DocumentDownloadModel) -> Unit)? = null

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
            binding.tvTitle.text = binding.root.context.getString(R.string.document_opened_title)
            binding.tvNumber.text = "#${model.evrotrustTransactionId}"
            binding.tvFileName.text = model.fileName
            binding.tvCreatedOnValue.text = model.created.convertDate()
            model.signed?.let {
                binding.tvSignedOnTitle.text = StringSource.Res(R.string.document_signed_on).getString(binding.root.context)
                binding.tvSignedOnValue.text = it.convertDate()
            }
            model.rejected?.let {
                binding.tvSignedOnTitle.text = StringSource.Res(R.string.document_rejected_on).getString(binding.root.context)
                binding.tvSignedOnValue.text = it.convertDate()
            }
            model.expired?.let {
                binding.tvSignedOnTitle.text = StringSource.Res(R.string.document_expired_on).getString(binding.root.context)
                binding.tvSignedOnValue.text = it.convertDate()
            }
            binding.tvShowFile.isVisible = model.fileUrl.isNotEmpty()
            binding.tvDownloadFile.isVisible = model.fileUrl.isNotEmpty()
            val btnStatusColor = when (model.status) {
                DocumentStatusModel.UNSIGNED,
                DocumentStatusModel.SIGNED -> R.drawable.bg_ripple_green_button_states

                DocumentStatusModel.SIGNING,
                DocumentStatusModel.PENDING -> R.drawable.bg_ripple_blue_button_states

                DocumentStatusModel.REJECTED,
                DocumentStatusModel.EXPIRED,
                DocumentStatusModel.FAILED,
                DocumentStatusModel.WITHDRAWN,
                DocumentStatusModel.UNKNOWN -> R.drawable.bg_ripple_orange_button_states
            }
            binding.btnStatus.setBackgroundDrawableResource(btnStatusColor)
            val btnTextRes = when (model.status) {
                DocumentStatusModel.SIGNED -> R.string.document_status_signed
                DocumentStatusModel.SIGNING -> R.string.document_status_wait_sign
                DocumentStatusModel.EXPIRED -> R.string.document_status_expired
                DocumentStatusModel.REJECTED -> R.string.document_status_rejected
                DocumentStatusModel.PENDING -> R.string.document_status_pending
                DocumentStatusModel.UNSIGNED -> R.string.document_status_unsigned
                DocumentStatusModel.FAILED -> R.string.document_status_failed
                DocumentStatusModel.WITHDRAWN -> R.string.document_status_withdrawn
                DocumentStatusModel.UNKNOWN -> R.string.document_status_unknown

            }
            binding.btnStatus.setTextResource(btnTextRes)
            @DrawableRes val btnIcon = when (model.status) {
                DocumentStatusModel.UNSIGNED,
                DocumentStatusModel.SIGNED -> R.drawable.ic_ok

                DocumentStatusModel.SIGNING,
                DocumentStatusModel.PENDING -> R.drawable.ic_wait_white

                DocumentStatusModel.UNKNOWN,
                DocumentStatusModel.REJECTED,
                DocumentStatusModel.FAILED,
                DocumentStatusModel.WITHDRAWN,
                DocumentStatusModel.EXPIRED -> R.drawable.ic_close_white
            }
            binding.btnStatus.setCompoundDrawablesExt(start = btnIcon)
            binding.tvFileName.onClickThrottle {
                showClickListener?.invoke(
                    DocumentDownloadModel(
                        name = model.fileName,
                        url = model.fileUrl
                    )
                )
            }
            binding.tvShowFile.onClickThrottle {
                showClickListener?.invoke(
                    DocumentDownloadModel(
                        name = model.fileName,
                        url = model.fileUrl
                    )
                )
            }
            binding.tvDownloadFile.onClickThrottle {
                downloadClickListener?.invoke(
                    DocumentDownloadModel(
                        name = model.fileName,
                        url = model.fileUrl
                    )
                )
            }
        }
    }

}