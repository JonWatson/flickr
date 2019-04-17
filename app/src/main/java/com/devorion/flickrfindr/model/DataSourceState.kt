package com.devorion.flickrfindr.model

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.devorion.flickrfindr.model.api.NetworkSearchLiveData
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.state.ServiceState

// Data class meant to be contained in a ViewModel and observed by the UI
data class DataSourceState(
    val dataSourceState: LiveData<NetworkSearchLiveData>,

    // Links to the DataSource to invoke retry/refresh
    val retry: () -> Unit,
    val refresh: () -> Unit,
    val loadMore: () -> Unit
)