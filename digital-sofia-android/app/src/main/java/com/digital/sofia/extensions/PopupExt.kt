/**
 * Creates and shows a list of [items] in drop down popup window.
 * [dropDownAnimationAnchor] used to calculate an drop down animation start point in the screen.
 * [dropDownInputAnchor] used to calculate the max width of drop down which is cannot be more
 * than [dropDownInputAnchor].
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.extensions

import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import com.digital.sofia.R
import kotlin.math.max

fun <T> Fragment.showListPopupDropDownWindow(
    dropDownAnimationAnchor: View,
    dropDownInputAnchor: View,
    dropDownArrayAdapter: ArrayAdapter<T>,
    items: List<T>,
    onItemClickListener: (position: Int)->Unit
): ListPopupWindow {
    return ListPopupWindow(
        requireContext(), null, 0, R.style.CustomListPopupWindowStyle
    ).apply {
        anchorView = dropDownAnimationAnchor
        setDropDownGravity(Gravity.END)
        setAdapter(dropDownArrayAdapter)
        setOnItemClickListener { _, _, position, _ ->
            onItemClickListener.invoke(position)
            dismiss()
        }
        dropDownArrayAdapter.swapItems(items)
        measureAndApplyMaxWidth(dropDownInputAnchor, dropDownArrayAdapter)
        show()
    }
}

/**
 * Measures all items in the list and apply the max width as the width
 * of popup menu. The max width cannot be more than [anchor] measuredWidth.
 * This method works only for single item view type in [listAdapter].
 */
private fun ListPopupWindow.measureAndApplyMaxWidth(anchor: View, listAdapter: ArrayAdapter<*>) {
    val maxWidth = measureMaxWidth(anchor, listAdapter)
    width = if (maxWidth >= anchor.measuredWidth) {
        anchor.measuredWidth
    } else {
        maxWidth
    }
}

private fun measureMaxWidth(anchor: View, listAdapter: ArrayAdapter<*>): Int {
    var maxWidth = 0
    var itemView: View? = null
    val measureTempLayout = FrameLayout(anchor.context)
    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    for (position in 0 until listAdapter.count) {
        measureTempLayout.removeAllViews()
        itemView = listAdapter.getView(position, itemView, measureTempLayout)
        itemView.measure(widthMeasureSpec, heightMeasureSpec)
        maxWidth = max(maxWidth, itemView.measuredWidth)
    }
    return maxWidth
}

fun <T> ArrayAdapter<T>.swapItems(list: List<T>) {
    clear()
    addAll(list)
    notifyDataSetChanged()
}