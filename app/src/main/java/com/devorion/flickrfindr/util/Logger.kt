package com.devorion.flickrfindr.util

import android.util.Log

class Logger(private val tag: String) {

    companion object {
        @JvmStatic
        fun getLogger(clazz: Class<*>) = Logger(clazz.simpleName)

        @JvmStatic
        fun getLogger(clazz: Class<*>, extra: String) = Logger("${clazz.simpleName}($extra)")
    }

    fun v(string: String) {
        Log.v(tag, string)
    }

    fun d(string: String) {
        Log.d(tag, string)
    }

    fun i(string: String) {
        Log.i(tag, string)
    }

    fun w(string: String) {
        Log.w(tag, string)
    }

    fun e(string: String) {
        Log.e(tag, string)
    }
}