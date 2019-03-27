package com.devorion.flickrfindr.util

import android.content.Context
import android.preference.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    context: Context
) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getFullSizeImagesPref(): Boolean {
        return sharedPreferences.getBoolean(PREF_FULL_IMAGES, false)
    }

    fun setFullSizeImagesPref(full: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_FULL_IMAGES, full).apply()
    }

    companion object {
        const val PREF_FULL_IMAGES = "full"
    }
}