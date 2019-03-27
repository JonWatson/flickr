package com.devorion.flickrfindr.util

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity

class ActivityUtils {
    companion object {
        fun getActivityFromContext(context: Context): AppCompatActivity = context as? AppCompatActivity
            ?: getActivityFromContext((context as ContextWrapper).baseContext)
    }
}