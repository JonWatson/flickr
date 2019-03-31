package com.devorion.flickrfindr.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.devorion.flickrfindr.di.DaggerTestComponent
import com.devorion.flickrfindr.di.TestModule
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.model.state.ServiceState
import com.devorion.flickrfindr.model.state.Status
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import javax.inject.Inject

@RunWith(MockitoJUnitRunner::class)
class NetworkViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject
    lateinit var flickrService: FakeFlickrService

    @Mock
    lateinit var stateObserver: Observer<ServiceState>

    @Mock
    lateinit var photoObserver: Observer<PagedList<Photo>>

    @Before
    fun setUp() {
        val component = DaggerTestComponent.builder()
            .testModule(TestModule()).build()
        component.inject(this)
    }

    @Test
    fun searchTriggersInitialLoadState() {
        flickrService.addPhotos("test", 200)
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)

        networkViewModel.updateSearchText("test")
        verify(stateObserver).onChanged(ServiceState(Status.LOADING, true, null))
    }

    @Test
    fun searchTriggersInitialSuccessState() {
        flickrService.addPhotos("test", 200)
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)

        networkViewModel.updateSearchText("test")
        verify(stateObserver).onChanged(ServiceState(Status.SUCCESS, true, null))
    }

    @Test
    fun loadSecondPageTriggersLoadingState() {
        flickrService.addPhotos("test", 200)
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)

        networkViewModel.updateSearchText("test")
        networkViewModel.photos.value?.loadAround(20)   // load another page
        verify(stateObserver).onChanged(ServiceState(Status.LOADING, false, null))
    }

    @Test
    fun loadSecondPageTriggersSuccessState() {
        flickrService.addPhotos("test", 200)
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)

        networkViewModel.updateSearchText("test")
        networkViewModel.photos.value?.loadAround(20)   // load another page
        verify(stateObserver).onChanged(ServiceState(Status.SUCCESS, false, null))
    }

    @Test
    fun emptySearchReturnsSuccess() {
        flickrService.addPhotos("test", 200)
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)

        networkViewModel.updateSearchText("nope")
        verify(stateObserver).onChanged(ServiceState(Status.SUCCESS, true, null))
    }

    @Test
    fun searchFailureReturnsFailureState() {
        flickrService.addPhotos("test", 200)
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)
        flickrService.failWithMessage = "Failed"

        networkViewModel.updateSearchText("test")
        verify(stateObserver).onChanged(ServiceState(Status.FAILED, true, "Failed"))

        flickrService.failWithMessage = null
    }

    @Test
    fun retryReturnsLoadingState() {
        flickrService.addPhotos("test", 200)
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)
        flickrService.failWithMessage = "Failed"

        networkViewModel.updateSearchText("test")
        verify(stateObserver).onChanged(ServiceState(Status.FAILED, true, "Failed"))

        flickrService.failWithMessage = null

        networkViewModel.retryLastFailedPage()
        verify(stateObserver).onChanged(ServiceState(Status.LOADING, true, null))
    }
}
