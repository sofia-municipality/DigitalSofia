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
import androidx.core.content.withStyledAttributes
import com.digital.sofia.R
import com.digital.sofia.databinding.LayoutLoaderBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug

class LoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LoaderViewTag"
    }

    private val binding = LayoutLoaderBinding.inflate(LayoutInflater.from(context), this)

    init {
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.LoaderView) {
            getString(R.styleable.LoaderView_loader_view_message)?.let {
                logDebug("tvMessage text: $it", TAG)
                binding.tvMessage.text = it
            }
        }
    }

    fun setMessage(message: String) {
        binding.tvMessage.text = message
    }

}