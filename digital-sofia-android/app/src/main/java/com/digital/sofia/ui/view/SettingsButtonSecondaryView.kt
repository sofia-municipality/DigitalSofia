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
import com.digital.sofia.databinding.LayoutSettingsButtonSecondaryViewBinding
import com.digital.sofia.extensions.onClickThrottle

class SettingsButtonSecondaryView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding =
        LayoutSettingsButtonSecondaryViewBinding.inflate(LayoutInflater.from(context), this)

    var clickListener: (() -> Unit)? = null

    init {
        setupAttributes(attrs)
        setupClickListeners()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.SettingsButtonSecondaryView) {
            getString(R.styleable.SettingsButtonSecondaryView_settings_button_secondary_view_title)?.let {
                binding.tvTitle.text = it
            }
            getDrawable(R.styleable.SettingsButtonSecondaryView_settings_button_secondary_view_icon)?.let {
                binding.ivIcon.setImageDrawable(it)
            }
        }
    }

    private fun setupClickListeners() {
        binding.root.onClickThrottle { clickListener?.invoke() }
    }

}