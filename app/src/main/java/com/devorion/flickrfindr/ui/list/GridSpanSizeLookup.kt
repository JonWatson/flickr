package com.devorion.flickrfindr.ui.list

import androidx.recyclerview.widget.GridLayoutManager
import com.devorion.flickrfindr.R

class GridSpanSizeLookup(
    private val adapter: PhotosAdapter,
    private val photoSpanSize: Int,
    private val totalSpanSize: Int
) : GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int): Int {
        return if (adapter.getItemViewType(position) == R.layout.photo_item) {
            totalSpanSize / photoSpanSize
        } else {    // Network Item
            totalSpanSize
        }
    }
}