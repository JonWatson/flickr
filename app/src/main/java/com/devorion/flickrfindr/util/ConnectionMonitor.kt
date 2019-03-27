package com.devorion.flickrfindr.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION")    // CONNECTIVITY_ACTION Might be deprecated but I was having some issues with the latest APIs(TODO)
@Singleton
class ConnectionMonitor @Inject constructor(
    context: Context
) : BroadcastReceiver() {

    private val LOG = Logger.getLogger(ConnectionMonitor::class.java)
    private val connectivityManager: ConnectivityManager =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    val networkLiveData = MutableLiveData<ConnectionStatus>()

    init {
        val intentFilter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        context.registerReceiver(this, intentFilter)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        checkNetworkConnection()
    }

    private fun checkNetworkConnection() {
        networkLiveData.postValue(
            if (connectivityManager.activeNetworkInfo?.isConnected == true)
                ConnectionStatus.CONNECTED
            else
                ConnectionStatus.DISCONNECTED
        )
    }

    enum class ConnectionStatus {
        CONNECTED,
        DISCONNECTED
    }
}