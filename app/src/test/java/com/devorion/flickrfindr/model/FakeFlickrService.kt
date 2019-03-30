package com.devorion.flickrfindr.model

import com.devorion.flickrfindr.model.api.FlickrService
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.pojo.Photos
import com.devorion.flickrfindr.model.pojo.SearchResponse
import io.reactivex.Single

class FakeFlickrService: FlickrService {
    val photos = mutableMapOf<String, MutableList<Photos>>()

    fun addPhotos(searchText: String, numPages: Int, pageSize: Int) {
        photos.getOrPut(searchText) {
            MutableList<Photos>(numPages) {
                createPhotos(searchText, pageSize, it, numPages, numPages * pageSize)
            }
        }
    }
    override fun search(text: String, pageSize: Int, page: Int): Single<SearchResponse> {
        
    }

    fun createPhoto(searchText: String): Photo {
        return Photo("id", "title", "farm", "server", "secret")
    }

    fun createPhotos(searchText: String, pageSize: Int, pageIndex: Int, numPages: Int, total: Int): Photos {
        val photoList = MutableList<Photo>(pageSize) {
            createPhoto(searchText)
        }
        return Photos(pageIndex, numPages, pageSize, total, photoList)
    }

}