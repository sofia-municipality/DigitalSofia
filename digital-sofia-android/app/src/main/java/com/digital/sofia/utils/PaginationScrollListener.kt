package com.digital.sofia.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class PaginationScrollListener(
    private val layoutManager: LayoutManager
): RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount: Int = layoutManager.childCount
        val totalItemCount: Int = layoutManager.itemCount
        var firstVisibleItemPosition = 0
        when (layoutManager) {
            is StaggeredGridLayoutManager -> {
                val firstVisibleItemPositions =
                    layoutManager.findFirstVisibleItemPositions(null)
                firstVisibleItemPosition = firstVisibleItemPositions[0]
            }
            is GridLayoutManager -> {
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            }
            is LinearLayoutManager -> {
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            }
        }
        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
            ) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean
}