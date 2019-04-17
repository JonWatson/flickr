package com.devorion.flickrfindr.model.api

import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.state.ServiceState

class NetworkSearchLiveData(
    val photos: List<Photo>,
    val serviceState: ServiceState
)