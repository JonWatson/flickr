package com.devorion.flickrfindr.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.devorion.flickrfindr.model.api.FlickrService
import com.devorion.flickrfindr.model.api.NetworkSearchPhotoDataSourceNew
import com.devorion.flickrfindr.model.state.Status
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
            val dataSource = NetworkSearchPhotoDataSourceNew(it, flickrService, compositeDisposable, PAGE_SIZE)

            val dataSourceState = DataSourceState(
                dataSourceState = dataSource.networkSearchLiveData,
                retry = { dataSource.retryLastFailedRequest() },
                refresh = { dataSource.loadInitial() },
                loadMore = { dataSource.loadAfter(false) }
            )

            dataSource.loadInitial()
            dataSourceState
        }

    // Each time networkLiveData is updated from a new search(above),
    // photos and networkState switch to pointing at the LiveData of the new DataSource
    val data = Transformations.switchMap(networkLiveData) { it.dataSourceState }

    fun updateSearchText(searchText: String): Boolean {
        if (this.searchText.value == searchText) {
            return false
        }
        this.searchText.value = searchText
        return true
    }

//    private fun imagesForSearchText(
//        searchText: String,
//        pageSize: Int,
//        compositeDisposable: CompositeDisposable
//    ): DataSourceState {
//
//
//        return DataSourceState(
//            photoList = dataSource.photosLiveData,
//            networkState = dataSource.networkState,
//            retry = { dataSource.retryLastFailedRequest() },
//            refresh = { dataSource.loadInitial() }
//        )
//    }

    fun loadMore() {
        if (data.value?.serviceState?.status == Status.SUCCESS) {
            networkLiveData.value?.loadMore?.invoke()
        }
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