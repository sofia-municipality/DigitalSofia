package com.digital.sofia.models.common

import android.net.NetworkCapabilities

data class CurrentNetwork(
    val isListening: Boolean,
    val networkCapabilities: NetworkCapabilities?,
    val isAvailable: Boolean,
    val isReachable: Boolean,
    val isBlocked: Boolean,
)

fun CurrentNetwork.isConnected(): Boolean {
    return isListening &&
            isAvailable &&
            isReachable &&
            !isBlocked &&
            networkCapabilities.isNetworkCapabilitiesValid()
}

private fun NetworkCapabilities?.isNetworkCapabilitiesValid(): Boolean = when {
    this == null -> false
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) -> true
    else -> false
}