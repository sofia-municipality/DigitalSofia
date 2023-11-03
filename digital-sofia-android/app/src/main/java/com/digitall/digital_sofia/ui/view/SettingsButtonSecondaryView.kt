package com.digitall.digital_sofia.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.LayoutSettingsButtonSecondaryViewBinding
import com.digitall.digital_sofia.extensions.onClickThrottle

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

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