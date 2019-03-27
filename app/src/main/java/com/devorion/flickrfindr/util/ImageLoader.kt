package com.devorion.flickrfindr.util

import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.devorion.flickrfindr.R
import com.devorion.flickrfindr.model.pojo.Photo
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoader @Inject constructor(
    private val glide: RequestManager,
    private val bookmarkManager: BookmarkManager
) {
    fun loadPhotoIntoImage(
        photo: Photo,
        imageView: ImageView,
        width: Int,
        height: Int,
        crop: Boolean = false,
        fade: Boolean = false
    ) {
        val requestCreator = if (bookmarkManager.isBookmarked(photo)) {
            glide.load(File(bookmarkManager.getBookmarkPathForPhoto(photo)))
        } else {
            glide.load(photo.getImageUrl())
        }

        requestCreator.override(width, height)
            .placeholder(ContextCompat.getDrawable(imageView.context, R.color.light_gray)!!)
            .error(ContextCompat.getDrawable(imageView.context, R.drawable.ic_error)!!)
            .apply {
                if (fade) {
                    transition(DrawableTransitionOptions.withCrossFade())
                }
            }
            .apply {
                if (crop) {
                    centerCrop()
                }
            }
            .into(imageView)
    }
}
