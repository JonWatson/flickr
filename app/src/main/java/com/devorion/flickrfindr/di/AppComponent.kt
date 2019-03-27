package com.devorion.flickrfindr.di

import com.devorion.flickrfindr.ui.DetailActivity
import com.devorion.flickrfindr.ui.MainActivity
import com.devorion.flickrfindr.ui.BookmarkActivity
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class, ViewModelModule::class])
@Singleton
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: DetailActivity)
    fun inject(activity: BookmarkActivity)
}