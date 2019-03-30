package com.devorion.flickrfindr.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.devorion.flickrfindr.di.DaggerTestComponent
import com.devorion.flickrfindr.di.TestModule
import com.devorion.flickrfindr.model.api.FlickrService
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
    lateinit var flickrService: FlickrService

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
    fun searchTriggersLoadingState() {
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)
        networkViewModel.updateSearchText("test")
        verify(stateObserver).onChanged(ServiceState(Status.RUNNING, true, null))
    }

    @Test
    fun searchTriggersSuccessState() {
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)
        networkViewModel.updateSearchText("test")
        verify(stateObserver).onChanged(ServiceState(Status.SUCCESS, true, null))
    }


    @Test
    fun searchLoadSecondPage() {
        val networkViewModel = NetworkViewModel(flickrService)
        networkViewModel.networkState.observeForever(stateObserver)
        networkViewModel.photos.observeForever(photoObserver)
        networkViewModel.updateSearchText("test")
        networkViewModel.photos.value?.loadAround(23)
        verify(stateObserver).onChanged(ServiceState(Status.RUNNING, false, null))
    }
}
