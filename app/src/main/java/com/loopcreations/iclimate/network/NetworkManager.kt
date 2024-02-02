package com.loopcreations.iclimate.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkManager(context: Context) {

    private var connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    var isCallbackRegistered = false
    private val _networkStatus = MutableLiveData<Boolean>()
    val networkStatus: LiveData<Boolean> = _networkStatus

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _networkStatus.postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _networkStatus.postValue(false)
        }
    }

    fun checkNetwork(): Boolean{
        val network = connectivityManager.activeNetwork
        if(network == null){
            _networkStatus.postValue(false)
            return false
        }
        return true
    }

    fun registerNetworkCallback(){
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        isCallbackRegistered = true
    }

    fun unregisterNetworkCallback(){
        connectivityManager.unregisterNetworkCallback(networkCallback)
        isCallbackRegistered = false
    }
}