package com.digitall.digital_sofia.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.LayoutErrorBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.extensions.onClickThrottle

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "ErrorViewTag"
    }

    private val binding = LayoutErrorBinding.inflate(LayoutInflater.from(context), this)

    var reloadClickListener: (() -> Unit)? = null

    init {
        setupAttributes(attrs)
        setupControls()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.ErrorView) {
            getBoolean(R.styleable.ErrorView_error_view_show_title, true).let {
                logDebug("tvErrorViewTitle isVisible: $it", TAG)
                binding.tvErrorViewTitle.isVisible = it
            }
            getBoolean(R.styleable.ErrorView_error_view_show_description, true).let {
                logDebug("tvErrorViewDescription isVisible: $it", TAG)
                binding.tvErrorViewDescription.isVisible = it
            }
            getString(R.styleable.ErrorView_error_view_title)?.let {
                logDebug("tvErrorViewTitle text: $it", TAG)
                binding.tvErrorViewTitle.text = it
            }
            getString(R.styleable.ErrorView_error_view_description)?.let {
                logDebug("tvErrorViewDescription text: $it", TAG)
                binding.tvErrorViewDescription.text = it
            }
            getString(R.styleable.ErrorView_error_view_button_title)?.let {
                logDebug("btnErrorViewReload text: $it", TAG)
                binding.btnErrorViewReload.text = it
            }
            getDrawable(R.styleable.ErrorView_error_view_icon)?.let {
                binding.ivErrorIcon.setImageDrawable(it)
            }
        }
    }

    private fun setupControls() {
        binding.btnErrorViewReload.onClickThrottle {
            reloadClickListener?.invoke()
        }
    }

}