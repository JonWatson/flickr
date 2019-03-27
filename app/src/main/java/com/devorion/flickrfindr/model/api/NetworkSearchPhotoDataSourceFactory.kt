package com.devorion.flickrfindr.model.api

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.devorion.flickrfindr.model.pojo.Photo
import io.reactivex.disposables.CompositeDisposable

// Creates the Network DataSource and posts it's value for the observing ViewModel
class NetworkSearchPhotoDataSourceFactory(
    private val searchText: String,
    private val flickrService: FlickrService,
    private val compositeDisposable: CompositeDisposable
) : DataSource.Factory<Int, Photo>() {

    val networkSearchPhotoDataSourceLiveData = MutableLiveData<NetworkSearchPhotoDataSource>()

    override fun create(): DataSource<Int, Photo> {
        val source = NetworkSearchPhotoDataSource(
            searchText,
            flickrService,
            compositeDisposable
        )
        networkSearchPhotoDataSourceLiveData.postValue(source)
        return source
    }
}