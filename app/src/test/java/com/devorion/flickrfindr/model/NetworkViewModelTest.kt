package com.devorion.flickrfindr.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.devorion.flickrfindr.di.DaggerTestComponent
import com.devorion.flickrfindr.di.TestModule
import com.devorion.flickrfindr.model.api.FlickrService
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
    lateinit var observer: Observer<ServiceState>

    @Before
    fun setUp() {
        val component = DaggerTestComponent.builder()
            .testModule(TestModule()).build()
        component.inject(this)
    }

    // WIP
    @Test
    fun searchTriggersLoadingState() {
        val networkViewModel = NetworkViewModel(flickrService)

        networkViewModel.networkState.observeForever(observer)
        networkViewModel.updateSearchText("test")
        verify(observer).onChanged(ServiceState(Status.LOADING, true, null))
    }

}
