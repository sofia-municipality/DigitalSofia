/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.digital.sofia.databinding.LayoutEmptyStateBinding
import com.digital.sofia.extensions.onClickThrottle

class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutEmptyStateBinding.inflate(LayoutInflater.from(context), this)

    var reloadClickListener: (() -> Unit)? = null

    init {
        setupControls()
    }

    private fun setupControls() {
        binding.btnEmptyStateViewReload.onClickThrottle { reloadClickListener?.invoke() }
    }

}