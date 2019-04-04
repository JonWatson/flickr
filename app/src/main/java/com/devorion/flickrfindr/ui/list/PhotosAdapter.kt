package com.devorion.flickrfindr.ui.list

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
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
) : PagedListAdapter<Photo, RecyclerView.ViewHolder>(PHOTO_COMPARATOR) {

    private var networkState: ServiceState? = null

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (getItemViewType(position)) {
            R.layout.photo_item -> (holder as PhotoViewHolder).bind(getItem(position))
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

    override fun getItemCount() =
        super.getItemCount() + if (networkState != null) 1 else 0

    // Adds or removes the network_state_item layout which only appears at the end of the list
    // It display a loading indicator or a retry button/error message after an error occurs
    fun addOrUpdateNetworkStatusCard(networkState: ServiceState) {
        val previousNetworkState = this.networkState
        this.networkState = networkState
        if (previousNetworkState == null) {
            notifyItemInserted(super.getItemCount())
        } else if (networkState != previousNetworkState){
            notifyItemChanged(super.getItemCount())
        }
    }

    fun removeNetworkStatusCard() {
        if (networkState != null) {
            networkState = null
            notifyItemRemoved(super.getItemCount())
        }
    }

    companion object {
        val PHOTO_COMPARATOR = object : DiffUtil.ItemCallback<Photo>() {
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem.id == newItem.id

            override fun getChangePayload(oldItem: Photo, newItem: Photo): Boolean =
                false
        }
    }
}

