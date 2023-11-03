package com.digitall.digital_sofia.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.digitall.digital_sofia.databinding.LayoutEmptyStateBinding
import com.digitall.digital_sofia.extensions.onClickThrottle

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

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