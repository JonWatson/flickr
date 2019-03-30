package com.devorion.flickrfindr.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.devorion.flickrfindr.model.api.FlickrService
import com.devorion.flickrfindr.model.api.NetworkSearchPhotoDataSourceFactory
import com.devorion.flickrfindr.model.pojo.Photo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

// Android ViewModel that bridges the Network Data and UI
class NetworkViewModel @Inject internal constructor(
    private val flickrService: FlickrService
) : ViewModel() {

    private val searchText = MutableLiveData<String>()
    private val compositeDisposable = CompositeDisposable()

    // Every search creates a new DataSource based on the new Search term. The following is triggered
    // each time searchText(LiveData) is modified, clearing any ongoing requests and creating the new DataSource
    private val networkLiveData =
        Transformations.map(searchText) {
            compositeDisposable.clear() // Dispose of any ongoing requests before creating a new Data Source
            imagesForSearchText(it, PAGE_SIZE, compositeDisposable)
        }

    // Each time networkLiveData is updated from a new search(above),
    // photos and networkState switch to pointing at the LiveData of the new DataSource
    val photos = Transformations.switchMap(networkLiveData) { it.pagedList }
    val networkState = Transformations.switchMap(networkLiveData) { it.networkState }

    fun updateSearchText(searchText: String): Boolean {
        if (this.searchText.value == searchText) {
            return false
        }
        this.searchText.value = searchText
        return true
    }

    private fun imagesForSearchText(
        searchText: String,
        pageSize: Int,
        compositeDisposable: CompositeDisposable
    ): DataSourceState<Photo> {

        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setPrefetchDistance(5)             // arbitrary, but it defaults to pageSize which would auto-load second page(against spec)
            .setInitialLoadSizeHint(pageSize)   // see above, spec says to load 25 items initially, this defaults to pageSize * 3 if not set
            .setEnablePlaceholders(false)
            .build()

        val sourceFactory = NetworkSearchPhotoDataSourceFactory(
            searchText,
            flickrService,
            compositeDisposable
        )

        val pagedList = LivePagedListBuilder<Int, Photo>(sourceFactory, config).build()

        return DataSourceState(
            pagedList = pagedList,
            networkState = Transformations.switchMap(sourceFactory.networkSearchPhotoDataSourceLiveData) { it.networkState },
            retry = { sourceFactory.networkSearchPhotoDataSourceLiveData.value?.retryLastFailedRequest() },
            refresh = { sourceFactory.networkSearchPhotoDataSourceLiveData.value?.invalidate() }
        )
    }

    fun retryLastFailedPage() {
        networkLiveData.value?.retry?.invoke()
    }

    fun refresh(): Boolean {
        return networkLiveData.value?.refresh?.invoke() != null
    }

    override fun onCleared() {
        super.onCleared()
        // Dispose of any ongoing requests when the ViewModel is destroyed
        compositeDisposable.clear()
    }

    companion object {
        const val PAGE_SIZE = 25
    }
}