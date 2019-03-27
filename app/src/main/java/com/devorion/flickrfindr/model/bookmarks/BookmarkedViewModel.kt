package com.devorion.flickrfindr.model.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.devorion.flickrfindr.model.pojo.Photo

// Android ViewModel for bridging Bookmarked database data with the UI
class BookmarkedViewModel(
    bookmarkPhotoDao: BookmarkPhotoDao
) : ViewModel() {

    val bookmarkedPhotos: LiveData<PagedList<Photo>> =
        bookmarkPhotoDao.photosForPaging().toLiveData(pageSize = PAGE_SIZE)

    companion object {
        const val PAGE_SIZE = 25
    }
}