package com.devorion.flickrfindr.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.devorion.flickrfindr.R
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.ui.DetailActivity
import com.devorion.flickrfindr.util.ActivityUtils
import com.devorion.flickrfindr.util.BookmarkManager
import com.devorion.flickrfindr.util.ImageLoader
import com.devorion.flickrfindr.util.StartActivityThrottler

class PhotoViewHolder(
    view: View,
    private val imageLoader: ImageLoader,
    private val bookmarkManager: BookmarkManager,
    private val spanCount: Int,
    private val startActivityThrottler: StartActivityThrottler
) : RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.text)
    private val image: ImageView = view.findViewById(R.id.image)
    private val bookmarkIcon: ImageView = view.findViewById(R.id.bookmarkIcon)
    private val bookmarkButton: View = view.findViewById(R.id.bookmarkButton)

    private var photo: Photo? = null

    init {
        view.setOnClickListener {
            photo?.let {
                if (startActivityThrottler.okToStartActivity()) {
                    val intent = Intent(view.context, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXTRA_PHOTO, it)
                    startActivity(view.context, intent, Bundle.EMPTY)
                }
            }
        }

        bookmarkButton.setOnClickListener {
            photo?.let {
                bookmarkManager.toggleBookmark(it)
            }
        }

        bookmarkManager.bookmarkLiveData.observe(
            ActivityUtils.getActivityFromContext(title.context),
            Observer { list ->
                updateBookmark(list.contains(photo))
            })
    }

    fun bind(photo: Photo?) {
        this.photo = photo

        photo?.let {
            title.text = if (photo.title.isEmpty()) title.resources.getString(R.string.card_no_title) else photo.title
            val width = title.resources.displayMetrics.widthPixels / spanCount
            updateBookmark(bookmarkManager.isBookmarked(it))
            imageLoader.loadPhotoIntoImage(photo, image, width, width, crop = true, fade = true)
        }
    }

    private fun updateBookmark(bookmarked: Boolean) {
        bookmarkIcon.setImageResource(if (bookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            imageLoader: ImageLoader,
            bookmarkManager: BookmarkManager,
            spanCount: Int,
            startActivityThrottler: StartActivityThrottler
        ): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.photo_item, parent, false)
            return PhotoViewHolder(
                view,
                imageLoader,
                bookmarkManager,
                spanCount,
                startActivityThrottler
            )
        }
    }
}