package com.devorion.flickrfindr.model

import com.devorion.flickrfindr.model.api.FlickrService
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.pojo.Photos
import com.devorion.flickrfindr.model.pojo.SearchResponse
import io.reactivex.Single
import java.io.IOException

class FakeFlickrService : FlickrService {
    val photos = mutableMapOf<String, MutableList<Photo>>()
    var failWithMessage:String? = null

    fun addPhotos(searchText: String, total: Int) {
        photos.getOrPut(searchText) {
            MutableList(total) {
                createPhoto()
            }
        }
    }

    override fun search(text: String, pageSize: Int, page: Int): Single<SearchResponse> {
        failWithMessage?.let {
            return Single.error(IOException(failWithMessage))
        }

        val photoList = photos[text] ?: emptyList<Photo>()
        val pageIndex = page - 1
        val startItemIndex = pageIndex * pageSize
        val pageOfPhotos =
            if (photoList.isEmpty())
                photoList
            else
                photoList.subList(startItemIndex, Math.min(startItemIndex + pageSize, photoList.size)
        )
        val photosObject =
            createPhotos(
                page,
                (photoList.size + pageSize - 1) / pageSize,
                pageSize,
                photoList.size,
                pageOfPhotos
            )
        return Single.just(SearchResponse(photosObject, "ok"))
    }

    private fun createPhoto(): Photo {
        return Photo("id", "title", "farm", "server", "secret")
    }

    fun createPhotos(page: Int, numPages: Int, pageSize: Int, total: Int, photoList: List<Photo>): Photos {
        return Photos(page, numPages, pageSize, total, photoList)
    }
}