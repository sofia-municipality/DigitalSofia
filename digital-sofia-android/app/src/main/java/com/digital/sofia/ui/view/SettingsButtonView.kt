/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.content.withStyledAttributes
import com.digital.sofia.R
import com.digital.sofia.databinding.LayoutSettingsButtonViewBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.extensions.setTextResource

class SettingsButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding =
        LayoutSettingsButtonViewBinding.inflate(LayoutInflater.from(context), this)

    var clickListener: (() -> Unit)? = null

    init {
        setupAttributes(attrs)
        setupClickListeners()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.SettingsButtonView) {
            getString(R.styleable.SettingsButtonView_settings_button_view_title)?.let {
                binding.tvTitle.text = it
            }
            getDrawable(R.styleable.SettingsButtonView_settings_button_view_icon)?.let {
                binding.ivIcon.setImageDrawable(it)
            }
            val description =
                getString(R.styleable.SettingsButtonView_settings_button_view_description)
            if (description != null) {
                binding.tvDescription.text = description
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.root.onClickThrottle { clickListener?.invoke() }
    }

    fun setDescription(description: String) {
        binding.tvDescription.text = description
        binding.tvDescription.visibility = View.VISIBLE
    }

    fun setDescription(@StringRes descriptionRes: Int) {
        binding.tvDescription.setTextResource(descriptionRes)
        binding.tvDescription.visibility = View.VISIBLE
    }

}