package com.ale.bugreport

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var connectivityManager: ConnectivityManager? = null
    private val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .setIncludeOtherUidNetworks(true).build()
    private val logsBuffer = StringBuilder();

    private val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            postLogs("onAvailable()")
            unregisterNetworkCallback()
        }

        override fun onUnavailable() {
            postLogs("onUnavailable()")
            unregisterNetworkCallback()
        }
    }
    private var startTestBtn: Button? = null
    private var logs: TextView? = null
    private var wipeLogs: Button? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectivityManager =            this.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        logs = findViewById(R.id.logs)
        startTestBtn = findViewById(R.id.button)
        startTestBtn?.setOnClickListener {
            postLogs("Start test")
            coroutineScope.launch {
                connectivityManager?.requestNetwork(request, networkCallback)
            }
        }
        wipeLogs = findViewById(R.id.wipeLogs)
        wipeLogs?.setOnClickListener {
            logsBuffer.clear()
            logs?.text = ""
        }
    }

    private fun postLogs(logRow: String) {
        logsBuffer.append("$logRow \n")
        coroutineScope.launch {
            withContext(Dispatchers.Main) {
                logs?.text = logsBuffer
            }
        }
    }

    private fun unregisterNetworkCallback(){
        try {
            coroutineScope.launch {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            }
        }catch (e: Exception){
            //don't care
        }
    }
}