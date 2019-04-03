package com.devorion.flickrfindr.di

import com.devorion.flickrfindr.model.FakeFlickrService
import com.devorion.flickrfindr.model.api.FlickrService
import dagger.Provides
import javax.inject.Singleton

@dagger.Module
internal class TestModule {
    @Singleton
    @Provides
    fun providesFakeFlickrService(): FakeFlickrService {
        return FakeFlickrService()
    }
}