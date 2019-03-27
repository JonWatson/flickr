package com.devorion.flickrfindr.model.api

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.state.ServiceState
import com.devorion.flickrfindr.model.state.Status
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

// Fetches pages of Network.  Also stores actions and notifies state regarding retry/refresh
class NetworkSearchPhotoDataSource(
    private val searchText: String,
    private val flickrService: FlickrService
) : PageKeyedDataSource<Int, Photo>() {

    private var retryCompletable: Completable? = null
    val networkState: MutableLiveData<ServiceState> = MutableLiveData()

    fun retryLastFailedRequest() {
        retryCompletable?.subscribeOn(Schedulers.io())!!
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Photo>) {
        updateNetworkState(ServiceState(Status.LOADING, true))
        flickrService.search(searchText, params.requestedLoadSize, 1)
            .subscribeBy(
                onSuccess = { response ->
                    updateNetworkState(ServiceState(Status.SUCCESS, true))
                    retryCompletable = null
                    callback.onResult(
                        response.photos.photoList,
                        null,
                        response.photos.page + 1
                    )
                },
                onError = {
                    updateNetworkState(ServiceState(Status.FAILED, true, it.message))
                    retryCompletable = Completable.fromAction { loadInitial(params, callback) }
                }
            )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
        updateNetworkState(ServiceState(Status.LOADING, false))
        flickrService.search(searchText, params.requestedLoadSize, params.key)
            .subscribeBy(
                onSuccess = { response ->
                    updateNetworkState(ServiceState(Status.SUCCESS, false))
                    retryCompletable = null
                    callback.onResult(
                        response.photos.photoList,
                        params.key + 1
                    )
                },

                onError = {
                    updateNetworkState(
                        ServiceState(Status.FAILED, false, it.message)
                    )
                    retryCompletable = Completable.fromAction { loadAfter(params, callback) }
                }
            )
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
        // no-op
    }

    private fun updateNetworkState(networkState: ServiceState) {
        this.networkState.postValue(networkState)
    }
}
