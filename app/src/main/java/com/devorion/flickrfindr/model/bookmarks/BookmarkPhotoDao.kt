package com.devorion.flickrfindr.model.bookmarks

import androidx.paging.DataSource
import androidx.room.*
import com.devorion.flickrfindr.model.pojo.Photo

// Bookmark storage
@Dao
interface BookmarkPhotoDao {
    // Magically fills LiveData from Room DB into the ViewModel
    @Query("SELECT * FROM bookmarkedPhotos")
    fun photosForPaging(): DataSource.Factory<Int, Photo>

    @Query("SELECT * FROM bookmarkedPhotos")
    fun photos(): List<Photo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(photo: Photo)

    @Delete
    fun deleteBookmark(photo: Photo)
}