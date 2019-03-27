package com.devorion.flickrfindr.model.state

import com.devorion.flickrfindr.model.pojo.Photo

data class BookmarkState constructor(
    val status: Status,
    val photo: Photo,
    val operation: Operation,
    val msg: String? = null
) {
    enum class Operation {
        INSERT,
        REMOVE
    }
}