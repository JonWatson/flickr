package com.devorion.flickrfindr.ui.list

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val halfPadding: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = halfPadding
        outRect.right = halfPadding
        outRect.bottom = halfPadding
        outRect.top = halfPadding
    }
}

