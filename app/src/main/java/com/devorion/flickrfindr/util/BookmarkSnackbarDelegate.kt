package com.devorion.flickrfindr.util

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.devorion.flickrfindr.R
import com.devorion.flickrfindr.model.state.BookmarkState
import com.devorion.flickrfindr.model.state.Status
import com.google.android.material.snackbar.Snackbar

class BookmarkSnackbarDelegate {
    fun showBookmarkSnackbar(coordinatorLayout: CoordinatorLayout, bs: BookmarkState) {
        val message = when {
            bs.status == Status.SUCCESS && bs.operation == BookmarkState.Operation.INSERT -> R.string.bookmark_added
            bs.status == Status.SUCCESS && bs.operation == BookmarkState.Operation.REMOVE -> R.string.bookmark_removed
            bs.status == Status.FAILED && bs.operation == BookmarkState.Operation.INSERT -> R.string.bookmark_add_fail
            else -> R.string.bookmark_remove_fail
        }
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
    }
}