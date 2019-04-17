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
    val networkSearchLiveData: MutableLiveData<NetworkSearchLiveData> = MutableLiveData()
    private var numPagesLoaded = 0
    private var numPages = 0

    fun retryLastFailedRequest() {
        retryCompletable?.subscribeOn(Schedulers.io())!!
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun loadInitial() {
        numPagesLoaded = 0
        photos.clear()
        networkSearchLiveData.postValue(NetworkSearchLiveData(photos, ServiceState(Status.LOADING, true)))

        flickrService.search(searchText, pageSize, 1)
            .doOnSubscribe {
                compositeDisposable.add(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { response ->
                    retryCompletable = null
                    numPagesLoaded = 1
                    numPages = response.photos.numPages
                    photos.addAll(response.photos.photoList)
                    networkSearchLiveData.postValue(NetworkSearchLiveData(photos, ServiceState(Status.SUCCESS, true)))
                },
                onError = {
                    networkSearchLiveData.postValue(NetworkSearchLiveData(photos, ServiceState(Status.FAILED, true, it.message)))
                    retryCompletable = Completable.fromAction { loadInitial() }
                }
            )
    }

    fun loadAfter(fromRetry: Boolean) {
        if (!(numPagesLoaded > 0 && numPagesLoaded < numPages)) {
            LOG.w("No more pages to load")
            return
        }

        if (networkSearchLiveData.value?.serviceState?.status != Status.SUCCESS &&
            !(fromRetry && networkSearchLiveData.value?.serviceState?.status == Status.FAILED)) {
            LOG.w("Incorrect state to load more")
            return
        }


        networkSearchLiveData.postValue(NetworkSearchLiveData(photos, ServiceState(Status.LOADING, false)))
        flickrService.search(searchText, pageSize, numPagesLoaded + 1)
            .doOnSubscribe {
                compositeDisposable.add(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { response ->
                    retryCompletable = null
                    numPagesLoaded++
                    photos.addAll(response.photos.photoList)
                    networkSearchLiveData.postValue(NetworkSearchLiveData(photos, ServiceState(Status.SUCCESS, false)))
                },

                onError = {
                    networkSearchLiveData.postValue(NetworkSearchLiveData(photos, ServiceState(Status.FAILED, false, it.message)))
                    retryCompletable = Completable.fromAction { loadAfter(true) }
                }
            )
    }
}
