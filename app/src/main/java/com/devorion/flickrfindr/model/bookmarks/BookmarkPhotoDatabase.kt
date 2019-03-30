package com.devorion.flickrfindr.model.bookmarks

import androidx.room.RoomDatabase
import androidx.room.Database
import com.devorion.flickrfindr.model.pojo.Photo

@Database(entities = [Photo::class], version = 1, exportSchema = false)
abstract class BookmarkPhotoDatabase : RoomDatabase() {
    abstract fun bookmarkedPhotoDao(): BookmarkPhotoDao
}