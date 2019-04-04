@file:Suppress("DEPRECATION")

package com.devorion.flickrfindr.util

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.devorion.flickrfindr.model.bookmarks.BookmarkPhotoDatabase
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.state.BookmarkState
import com.devorion.flickrfindr.model.state.Status
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Helper that manages adding/removing Bookmarks and saving/retrieving saved images
// Entities can monitor this for changes made to Bookmarks
@Singleton
class BookmarkManager @Inject constructor(
    context: Context,
    bookmarkPhotoDatabase: BookmarkPhotoDatabase,
    private val glide: RequestManager
) {
    private val LOG = Logger.getLogger(BookmarkManager::class.java)

    private val bookmarkPhotoDao = bookmarkPhotoDatabase.bookmarkedPhotoDao()
    private val bookmarkList = ArrayList<Photo>()
    private val bookmarkDirectory = "${context.filesDir}/flickrfindr/"

    val bookmarkLiveData = MutableLiveData<ArrayList<Photo>>()
    val bookmarkServiceState = SingleLiveEvent<BookmarkState>() // one-shot

    init {
        Single.fromCallable {
            bookmarkPhotoDao.photos()
        }.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = {
                    bookmarkList.addAll(it)
                    bookmarkLiveData.postValue(bookmarkList)
                },
                onError = {
                    LOG.e("Error initializing bookmarks: ${it.message}")
                }
            )
    }

    fun isBookmarked(photo: Photo) = bookmarkList.contains(photo)
    fun getBookmarkPathForPhoto(photo: Photo) = "$bookmarkDirectory${photo.id}.jpg"

    fun toggleBookmark(photo: Photo) {
        if (bookmarkList.contains(photo)) {
            deleteBookmark(photo)
        } else {
            insertBookmark(photo)
        }
    }

    private fun insertBookmark(photo: Photo) {
        Single.fromCallable {
            bookmarkPhotoDao.insertBookmark(photo)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    bookmarkList.add(photo)
                    bookmarkLiveData.postValue(bookmarkList)
                    bookmarkServiceState.postValue(BookmarkState(Status.SUCCESS, photo, BookmarkState.Operation.INSERT))
                    downloadBookmarkedImage(photo)
                    LOG.d("Bookmarked Photo ${photo.id}")
                },
                onError = {
                    LOG.e("Error adding bookmark: ${it.message}")
                    bookmarkServiceState.postValue(BookmarkState(Status.FAILED, photo, BookmarkState.Operation.INSERT))
                }
            )
    }

    private fun deleteBookmark(photo: Photo) {
        Single.fromCallable {
            bookmarkPhotoDao.deleteBookmark(photo)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    bookmarkList.remove(photo)
                    bookmarkLiveData.postValue(bookmarkList)
                    bookmarkServiceState.postValue(BookmarkState(Status.SUCCESS, photo, BookmarkState.Operation.REMOVE))
                    deleteBookmarkedImage(photo)
                    LOG.d("Deleted Bookmark for Photo ${photo.id}")
                },
                onError = {
                    LOG.e("Error deleting bookmark: ${it.message}")
                    bookmarkServiceState.postValue(BookmarkState(Status.FAILED, photo, BookmarkState.Operation.REMOVE))
                }
            )
    }

    private fun downloadBookmarkedImage(photo: Photo) {
        glide.asBitmap()
            .load(photo.getImageUrl())
            .into(object : SimpleTarget<Bitmap>(
                DOWNLOAD_QUALITY,
                DOWNLOAD_QUALITY
            ) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val file = File(getBookmarkPathForPhoto(photo))
                    val directory = File(bookmarkDirectory)
                    try {
                        directory.mkdirs()
                        file.createNewFile()

                        val out = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.flush()
                        out.close()
                    } catch (e: IOException) {
                        LOG.e("Error downloading bookmark image for ${photo.getImageUrl()}: $e")
                        deleteBookmark(photo)
                    }

                }
            })
    }

    private fun deleteBookmarkedImage(photo: Photo) {
        try {
            File(getBookmarkPathForPhoto(photo)).delete()
        } catch (e: Exception) {
            LOG.e("Error deleting bookmark image for ${photo.getImageUrl()}: $e")
        }
    }

    companion object {
        const val DOWNLOAD_QUALITY = 800   // shortest side
    }
}