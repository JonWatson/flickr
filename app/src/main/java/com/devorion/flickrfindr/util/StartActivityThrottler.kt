package com.devorion.flickrfindr.util

import android.os.SystemClock

// Throttles spamming of touch events to disallow opening multiple activities at once
class StartActivityThrottler {
    private var lastStartActivityTime: Long = 0

    fun okToStartActivity(): Boolean {
        return (SystemClock.elapsedRealtime() - lastStartActivityTime >= CLICK_THROTTLE_MS).also {
            if (it) {
                lastStartActivityTime = SystemClock.elapsedRealtime()
            }
        }
    }

    companion object {
        const val CLICK_THROTTLE_MS = 1000
    }
}