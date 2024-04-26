package com.digital.sofia.ui.view

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.digital.sofia.R
import com.digital.sofia.databinding.LayoutBetaViewBinding
import com.digital.sofia.extensions.backgroundColor
import com.digital.sofia.models.common.StringSource

class BetaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "BetaViewTag"
    }

    var actionEmailClickListener: (() -> Unit)? = null

    private val binding = LayoutBetaViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        setupControls()
    }

    private fun setupControls() {
        val description = binding.tvBetaViewDescription.text
        val clickacbleString = StringSource.Res(R.string.beta_feedback_email).getString(context)
        val spanableString = SpannableString(description)
        val start = description.indexOf(clickacbleString)
        val end = start + clickacbleString.length

        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context, R.color.color_light_blue)
                ds.isUnderlineText = true
            }
            override fun onClick(view: View) {
                actionEmailClickListener?.invoke()
            }
        }

        spanableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvBetaViewDescription.text = spanableString
    }

}