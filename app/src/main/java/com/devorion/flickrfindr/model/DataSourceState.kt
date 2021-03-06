package com.devorion.flickrfindr.model

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.devorion.flickrfindr.model.state.ServiceState

// Data class meant to be contained in a ViewModel and observed by the UI
data class DataSourceState<T>(
    // LiveData PagedList data that powers the Adapter
    val pagedList: LiveData<PagedList<T>>,

    // The state of the DataSources network requests
    val networkState: LiveData<ServiceState>,

    // Links to the DataSource to invoke retry/refresh
    val retry: () -> Unit,
    val refresh: () -> Unit
)