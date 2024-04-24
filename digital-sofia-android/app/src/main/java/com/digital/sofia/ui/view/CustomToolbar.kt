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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.digital.sofia.R
import com.digital.sofia.databinding.LayoutToolbarBinding
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.extensions.pxDimen
import com.digital.sofia.extensions.setTextResource
import com.google.android.material.resources.TextAppearance

class CustomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CustomToolbarTag"
    }

    private val binding = LayoutToolbarBinding.inflate(LayoutInflater.from(context), this)

    var navigationClickListener: (() -> Unit)? = null

    var settingsClickListener: (() -> Unit)? = null

    init {
        setupView()
        setupAttributes(attrs)
        setupControls()
    }

    private fun setupView() {
        orientation = HORIZONTAL
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.CustomToolbar) {
            getString(R.styleable.CustomToolbar_toolbar_title)?.let {
                binding.tvTitle.text = it
            }
            val toolbarIcon = getDrawable(R.styleable.CustomToolbar_toolbar_icon)
            if (toolbarIcon != null) {
                binding.icNavigation.visibility = View.VISIBLE
                binding.icNavigation.setImageDrawable(toolbarIcon)
            }
            getBoolean(R.styleable.CustomToolbar_toolbar_show_settings, false).let {
                binding.icSettings.isVisible = it
            }
        }
    }

    fun showSettingsIcon(
        settingsClickListener: (() -> Unit),
    ) {
        binding.icSettings.visibility = View.VISIBLE
        this.settingsClickListener = settingsClickListener
    }

    fun showNavigationText(@StringRes textRes: Int, textAppearance: Int = R.style.TextStyle_20spRegularMainColor) {
        binding.tvTitle.setTextResource(textRes)
        binding.tvTitle.setTextAppearance(textAppearance)
    }

    fun showNavigationIcon(
        @DrawableRes iconRes: Int,
        navigationClickListener: (() -> Unit),
    ) {
        binding.icNavigation.visibility = View.VISIBLE
        binding.icNavigation.setImageResource(iconRes)
        this.navigationClickListener = navigationClickListener
    }

    private fun setupControls() {
        binding.layoutNavigation.onClickThrottle { navigationClickListener?.invoke() }
        binding.layoutSettings.onClickThrottle { settingsClickListener?.invoke() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = context.pxDimen(R.dimen.toolbar_height)
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

}