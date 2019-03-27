/* The MIT License (MIT)
 * Copyright (c) 2016 Pierre Degand
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.devorion.flickrfindr.ui.list

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class GridSpacingItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val (spanCount, spanIndex, spanSize) = extractGridData(
            parent, view
        )
        // SpanCount = 1 works great in the app, it's just not currently used
        if (spanCount == 1) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = (spacing * ((spanCount - spanIndex) / spanCount.toFloat())).toInt()
            outRect.right = (spacing * ((spanIndex + spanSize) / spanCount.toFloat())).toInt()
            outRect.bottom = spacing
        }
    }

    private fun extractGridData(parent: RecyclerView, view: View): GridItemData {
        val layoutManager = parent.layoutManager
        return when (layoutManager) {
            is GridLayoutManager -> extractGridLayoutData(layoutManager, view)
            is StaggeredGridLayoutManager -> extractStaggeredGridLayoutData(layoutManager, view)
            else -> throw UnsupportedOperationException("Bad layout params")
        }
    }

    private fun extractGridLayoutData(layoutManager: GridLayoutManager, view: View): GridItemData {
        val lp = view.layoutParams as GridLayoutManager.LayoutParams
        return GridItemData(
            layoutManager.spanCount,
            lp.spanIndex,
            lp.spanSize
        )
    }

    private fun extractStaggeredGridLayoutData(
        layoutManager: StaggeredGridLayoutManager,
        view: View
    ): GridItemData {
        val lp: StaggeredGridLayoutManager.LayoutParams =
            view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        return GridItemData(
            layoutManager.spanCount,
            lp.spanIndex,
            if (lp.isFullSpan) layoutManager.spanCount else 1
        )
    }

    internal data class GridItemData(val spanCount: Int, val spanIndex: Int, val spanSize: Int)
}

