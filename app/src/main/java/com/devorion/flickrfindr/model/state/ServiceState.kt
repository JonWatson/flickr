package com.devorion.flickrfindr.model.state

data class ServiceState(
    val status: Status,
    val isInitialLoad: Boolean,
    val msg: String? = null
)