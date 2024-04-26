package com.digital.sofia.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.CurrentNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

interface NetworkConnectionManager {

    val newNetworkConnectionChangeEventLiveData: LiveData<CurrentNetwork>

    fun startListenNetworkState()

    fun stopListenNetworkState()

    fun checkInternetConnection()

}

class NetworkConnectionManagerImpl(
    private val context: CurrentContext
): NetworkConnectionManager {

    companion object {
        private const val REACHABILITY_CHECK_INTERVAL = 30000L

        object InternetReachability {
            fun check(): Boolean {
                return try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                    socket.close()
                    true
                } catch (_: Exception) {
                    false
                }
            }

        }
    }

    private val connectivityManager = context.get().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = NetworkCallback()

    private var _currentNetwork = provideDefaultCurrentNetwork()

    private var reachabilityJob: Job? = null

    private val _newNetworkConnectionChangeEventLiveData = LiveEvent<CurrentNetwork>()
    override val newNetworkConnectionChangeEventLiveData = _newNetworkConnectionChangeEventLiveData.readOnly()

    override fun startListenNetworkState() {
        if (_currentNetwork.isListening) {
            return
        }

        _currentNetwork = _currentNetwork.copy(isListening = true)
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        startRepeatingReachabilityCheckJob()
    }

    override fun stopListenNetworkState() {
        if (!_currentNetwork.isListening) {
            return
        }

        _currentNetwork = _currentNetwork.copy(isListening = false)
        reachabilityJob?.cancel()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun checkInternetConnection() {
        _newNetworkConnectionChangeEventLiveData.setValueOnMainThread(_currentNetwork)
    }

    private fun provideDefaultCurrentNetwork(): CurrentNetwork {
        return CurrentNetwork(
            isListening = false,
            networkCapabilities = null,
            isAvailable = false,
            isReachable = false,
            isBlocked = false
        )
    }

    private fun startRepeatingReachabilityCheckJob() {
        reachabilityJob?.cancel()
        reachabilityJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val isInternetReachable = InternetReachability.check()
                _currentNetwork = _currentNetwork.copy(isReachable = isInternetReachable)
                _newNetworkConnectionChangeEventLiveData.setValueOnMainThread(_currentNetwork)
                delay(REACHABILITY_CHECK_INTERVAL)
            }
        }
    }

    private inner class NetworkCallback: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _currentNetwork = _currentNetwork.copy(isAvailable = true)
            startRepeatingReachabilityCheckJob()
            _newNetworkConnectionChangeEventLiveData.setValueOnMainThread(_currentNetwork)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            reachabilityJob?.cancel()
            _currentNetwork = _currentNetwork.copy(isAvailable = false, networkCapabilities = null)
            _newNetworkConnectionChangeEventLiveData.setValueOnMainThread(_currentNetwork)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            reachabilityJob?.cancel()
            _currentNetwork = _currentNetwork.copy(isAvailable = false, networkCapabilities = null)
            _newNetworkConnectionChangeEventLiveData.setValueOnMainThread(_currentNetwork)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            _currentNetwork = _currentNetwork.copy(networkCapabilities = networkCapabilities)
            _newNetworkConnectionChangeEventLiveData.setValueOnMainThread(_currentNetwork)
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            reachabilityJob?.cancel()
            if (!blocked) {
                startRepeatingReachabilityCheckJob()
            }
            _currentNetwork = _currentNetwork.copy(isBlocked = blocked)
            _newNetworkConnectionChangeEventLiveData.setValueOnMainThread(_currentNetwork)
        }
    }
}