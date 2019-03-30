package com.devorion.flickrfindr.model.api

import com.devorion.flickrfindr.model.pojo.SearchResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrService {
    // Note: Does safe_search even work?  That's a Big Yikes from me
    @GET("?method=flickr.photos.search&format=json&nojsoncallback=1&safe_search=1&&api_key=$FLICKR_API_KEY")
    fun search(
        @Query("text") text: String,
        @Query("per_page") pageSize: Int,
        @Query("page") page: Int
    ): Single<SearchResponse>

    companion object {
        const val FLICKR_API_KEY = "1508443e49213ff84d566777dc211f2a"
    }
}