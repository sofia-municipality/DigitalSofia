package com.digitall.digital_sofia.utils

import androidx.recyclerview.widget.DiffUtil
import com.digitall.digital_sofia.models.common.DiffEquals

/**
 * Simple Diff utils implementation. It will compare two items and
 * search for differences. Items should implement [DiffEquals] for
 * this callback.
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

class DefaultDiffUtilCallback<T: DiffEquals> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.isItemSame(newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.isContentSame(newItem)
    }

}