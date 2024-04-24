package com.digital.sofia.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.digital.sofia.databinding.LayoutNoInternetConnectionBinding
import com.digital.sofia.extensions.onClickThrottle

class NoInternetConnectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutNoInternetConnectionBinding.inflate(LayoutInflater.from(context), this)

}