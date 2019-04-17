package com.devorion.flickrfindr.ui.list

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devorion.flickrfindr.R
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.state.ServiceState
import com.devorion.flickrfindr.util.BookmarkManager
import com.devorion.flickrfindr.util.ImageLoader
import com.devorion.flickrfindr.util.StartActivityThrottler

class PhotosAdapter(
    private val imageLoader: ImageLoader,
    private val bookmarkManager: BookmarkManager,
    private val spanCount: Int,
    private val startActivityThrottler: StartActivityThrottler,
    private val retryCallback: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var networkState: ServiceState? = null
    private val photos: MutableList<Photo> = ArrayList()

    fun addPage(newPhotos: List<Photo>) {
        val curCount = itemCount
        this.photos.clear()
        this.photos.addAll(newPhotos)
        notifyItemRangeInserted(curCount, newPhotos.size)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (getItemViewType(position)) {
            R.layout.photo_item -> (holder as PhotoViewHolder).bind(photos[position])
            R.layout.network_state_item -> (holder as NetworkStateViewHolder).bindTo(networkState)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        onBindViewHolder(holder, position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.photo_item -> PhotoViewHolder.create(
                parent,
                imageLoader,
                bookmarkManager,
                spanCount,
                startActivityThrottler
            )
            R.layout.network_state_item -> NetworkStateViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun getItemViewType(
        position: Int
    ): Int {
        return if (networkState != null && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.photo_item
        }
    }

    override fun getItemCount() = photos.size + if (networkState != null) 1 else 0

    // Adds or removes the network_state_item layout which only appears at the end of the list
    // It display a loading indicator or a retry button/error message after an error occurs
    fun addOrUpdateNetworkStatusCard(networkState: ServiceState) {
        val previousNetworkState = this.networkState
        this.networkState = networkState
        if (previousNetworkState == null) {
            notifyItemInserted(itemCount - 1)
        } else if (networkState != previousNetworkState){
            notifyItemChanged(itemCount - 1)
        }
    }

    fun removeNetworkStatusCard() {
        if (networkState != null) {
            networkState = null
            notifyItemRemoved(itemCount)
        }
    }
}

