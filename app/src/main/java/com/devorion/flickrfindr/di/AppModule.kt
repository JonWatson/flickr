package com.devorion.flickrfindr.di

import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.devorion.flickrfindr.model.api.FlickrService
import com.devorion.flickrfindr.model.bookmarks.BookmarkPhotoDatabase
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

@dagger.Module
internal class AppModule(
    private val appContext: Context
) {
    @Singleton
    @Provides
    fun providesContext() = appContext

    @Singleton
    @Provides
    fun providesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        return Retrofit.Builder()
            .baseUrl("https://api.flickr.com/services/rest/")
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            // Simple 5MB disk cache for network requests
            val cacheSize = 5 * 1024 * 1024
            cache(Cache(context.cacheDir, cacheSize.toLong()))
        }.build()
    }

    @Singleton
    @Provides
    fun providesFlickrService(retrofit: Retrofit): FlickrService {
        return retrofit.create(FlickrService::class.java)
    }

    @Singleton
    @Provides
    fun providesOfflineDatabase(context: Context): BookmarkPhotoDatabase {
        return Room.databaseBuilder(
            context,
            BookmarkPhotoDatabase::class.java, "flickr_offline_db"
        ).build()
    }

    @Singleton
    @Provides
    fun providesGlide(context: Context): RequestManager {
        return Glide.with(context)
    }
}