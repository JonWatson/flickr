package com.devorion.flickrfindr

import android.app.Application
import com.devorion.flickrfindr.di.AppComponent
import com.devorion.flickrfindr.di.AppModule
import com.devorion.flickrfindr.di.DaggerAppComponent

class App : Application() {
    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }
}