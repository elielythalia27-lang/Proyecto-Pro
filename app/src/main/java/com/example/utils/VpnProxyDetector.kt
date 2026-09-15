package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.Collections

data class VpnProxyStatus(
    val isBlocked: Boolean = false,
    val isVpnDetected: Boolean = false,
    val isProxyDetected: Boolean = false,
    val reason: String = ""
)

object VpnProxyDetector {

    /**
     * Checks synchronously if a VPN connection or Proxy server is active on the device.
     */
    fun checkStatus(context: Context): VpnProxyStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        // 1. Check NetworkCapabilities TRANSPORT_VPN
        var vpnTransport = false
        try {
            val activeNetwork = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNetwork)
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                vpnTransport = true
            }
        } catch (_: Exception) {}

        // 2. Check Virtual Network Interfaces (TUN, TAP, PPP, P2P, etc.)
        var vpnInterface = false
        var interfaceName = ""
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                if (networkInterface.isUp) {
                    val name = networkInterface.name.lowercase()
                    if (name.startsWith("tun") ||
                        name.startsWith("ppp") ||
                        name.startsWith("p2p") ||
                        name.startsWith("tap") ||
                        name.contains("vpn")
                    ) {
                        vpnInterface = true
                        interfaceName = networkInterface.name
                        break
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Check System / Default Proxy settings
        var proxyDetected = false
        var proxyInfo = ""
        try {
            val httpHost = System.getProperty("http.proxyHost")
            val httpsHost = System.getProperty("https.proxyHost")
            val socksHost = System.getProperty("socksProxyHost")
            if (!httpHost.isNullOrBlank() || !httpsHost.isNullOrBlank() || !socksHost.isNullOrBlank()) {
                proxyDetected = true
                proxyInfo = (httpHost ?: httpsHost ?: socksHost) ?: "Proxy del sistema"
            }

            val defaultProxy = cm?.defaultProxy
            if (defaultProxy != null && !defaultProxy.host.isNullOrBlank()) {
                proxyDetected = true
                proxyInfo = defaultProxy.host
            }
        } catch (_: Exception) {}

        val isVpn = vpnTransport || vpnInterface
        val isBlocked = isVpn || proxyDetected

        val reason = when {
            isVpn && proxyDetected -> "VPN y Proxy detectados"
            isVpn -> if (interfaceName.isNotEmpty()) "VPN activa ($interfaceName)" else "Conexión VPN detectada"
            proxyDetected -> "Servidor Proxy activo ($proxyInfo)"
            else -> ""
        }

        return VpnProxyStatus(
            isBlocked = isBlocked,
            isVpnDetected = isVpn,
            isProxyDetected = proxyDetected,
            reason = reason
        )
    }

    /**
     * Fast check helper returning true if any VPN or Proxy is active.
     */
    fun isVpnOrProxyActive(context: Context): Boolean {
        return checkStatus(context).isBlocked
    }

    /**
     * Observes network changes and periodically re-validates VPN / Proxy status in real time.
     */
    fun observeVpnAndProxy(context: Context): Flow<VpnProxyStatus> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        // Send initial state immediately
        trySend(checkStatus(context))

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(checkStatus(context))
            }

            override fun onLost(network: Network) {
                trySend(checkStatus(context))
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(checkStatus(context))
            }
        }

        val request = NetworkRequest.Builder().build()
        try {
            cm?.registerNetworkCallback(request, callback)
        } catch (_: Exception) {}

        // Periodic ticker to catch interface changes that don't invoke standard network callbacks
        val tickerJob = launch {
            while (isActive) {
                delay(2000)
                trySend(checkStatus(context))
            }
        }

        awaitClose {
            tickerJob.cancel()
            try {
                cm?.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }.distinctUntilChanged()
}
