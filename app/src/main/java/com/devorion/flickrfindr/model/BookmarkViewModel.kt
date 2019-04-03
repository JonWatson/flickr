package com.devorion.flickrfindr.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.devorion.flickrfindr.model.bookmarks.BookmarkPhotoDatabase
import com.devorion.flickrfindr.model.pojo.Photo
import javax.inject.Inject

// Android ViewModel for bridging Bookmarked database data with the UI
class BookmarkViewModel @Inject internal constructor(
    bookmarkPhotoDatabase: BookmarkPhotoDatabase
) : ViewModel() {

    val bookmarkedPhotos: LiveData<PagedList<Photo>> =
        bookmarkPhotoDatabase.bookmarkedPhotoDao()
            .photosForPaging()
            .toLiveData(pageSize = PAGE_SIZE)

    companion object {
        const val PAGE_SIZE = 25
    }
}