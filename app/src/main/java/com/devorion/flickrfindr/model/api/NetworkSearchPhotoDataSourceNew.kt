package com.devorion.flickrfindr.model.api

import androidx.lifecycle.MutableLiveData
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.state.ServiceState
import com.devorion.flickrfindr.model.state.Status
import com.devorion.flickrfindr.util.Logger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

// Fetches pages of Network.  Also stores actions and notifies state regarding retry/refresh
class NetworkSearchPhotoDataSourceNew(
    private val searchText: String,
    private val flickrService: FlickrService,
    private val compositeDisposable: CompositeDisposable,
    private val pageSize: Int
) {

    private val LOG = Logger.getLogger(NetworkSearchPhotoDataSourceNew::class.java)
    private var retryCompletable: Completable? = null
    val photos: MutableList<Photo> = ArrayList()
    val photosLiveData: MutableLiveData<MutableList<Photo>> = MutableLiveData()
    val networkState: MutableLiveData<ServiceState> = MutableLiveData()
    private var numPagesLoaded = 0

    fun retryLastFailedRequest() {
        retryCompletable?.subscribeOn(Schedulers.io())!!
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun loadInitial() {
        numPagesLoaded = 0
        photos.clear()
        photosLiveData.value = photos
        updateNetworkState(ServiceState(Status.LOADING, true))

        flickrService.search(searchText, pageSize, 1)
            .doOnSubscribe {
                compositeDisposable.add(it)
            }
            .subscribeBy(
                onSuccess = { response ->
                    updateNetworkState(ServiceState(Status.SUCCESS, true))
                    retryCompletable = null
                    numPagesLoaded = 1
                    photos.addAll(response.photos.photoList)
                    photosLiveData.value = photos
                },
                onError = {
                    updateNetworkState(ServiceState(Status.FAILED, true, it.message))
                    retryCompletable = Completable.fromAction { loadInitial() }
                }
            )
    }

    fun loadAfter() {
        updateNetworkState(ServiceState(Status.LOADING, false))
        flickrService.search(searchText, pageSize, numPagesLoaded + 1)
            .doOnSubscribe {
                compositeDisposable.add(it)
            }
            .subscribeBy(
                onSuccess = { response ->
                    updateNetworkState(ServiceState(Status.SUCCESS, false))
                    retryCompletable = null
                    numPagesLoaded++
                    photos.addAll(response.photos.photoList)
                    photosLiveData.value = photos
                },

                onError = {
                    updateNetworkState(
                        ServiceState(Status.FAILED, false, it.message)
                    )
                    retryCompletable = Completable.fromAction { loadAfter() }
                }
            )
    }

    private fun updateNetworkState(networkState: ServiceState) {
        this.networkState.postValue(networkState)
    }
}
