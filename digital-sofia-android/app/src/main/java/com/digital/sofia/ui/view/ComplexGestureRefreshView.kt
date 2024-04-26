/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digital.sofia.R

class ComplexGestureRefreshView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    var isGestureAllowed = true

    private val helper = ComplexGestureTouchHelper()

    init {
        setProgressBackgroundColorSchemeResource(R.color.color_primary)
        setColorSchemeResources(R.color.color_primary)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (helper.onInterceptTouchEvent(event) && isGestureAllowed) {
            super.onInterceptTouchEvent(event)
        } else {
            false
        }
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        if (isGestureAllowed) {
            super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        }
    }

    override fun onNestedFling(
        target: View, velocityX: Float, velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return if (isGestureAllowed) {
            super.onNestedFling(target, velocityX, velocityY, consumed)
        } else false
    }
}