package pl.edu.agh.votingapp.comunication.client

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import pl.edu.agh.votingapp.comunication.NsdHelper
import pl.edu.agh.votingapp.viewmodel.join.OngoingVotings

class ServerDiscovery(
    val context: Context,
    val viewAdapter: RecyclerView.Adapter<*>,
    val votingList: OngoingVotings
) {

    val TAG = "BallotBull"
    val SERVICE_TYPE = "_ballotbull._tcp."

    var nsdHelper = NsdHelper(context)

    fun discoverServices() {
        nsdHelper.stopDiscovery() // Cancel any existing discovery request

        nsdHelper.initializeDiscoveryListener(object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "Service discovery success $service")
                when (service.serviceType) {
                    SERVICE_TYPE -> {
                        Log.d(TAG, "Service Name: " + service.serviceName)
                        votingList.addVoting(service)
                        viewAdapter.notifyDataSetChanged()
                    }
                    else -> {
                        Log.d(TAG, "Undefined Service Name: " + service.serviceName)
                    }
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.e(TAG, "Service lost $service")
                var removeIndex: Int? = null
                for ((index, voting) in votingList.getVotings().withIndex()) {
                    if (voting.name == service.serviceName) {
                        removeIndex = index
                        break
                    }
                }
                if (removeIndex != null) votingList.getVotings().removeAt(removeIndex)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(
                serviceType: String,
                errorCode: Int
            ) {
                Log.e(TAG, "Discovery failed: Error code: $errorCode")
            }

            override fun onStopDiscoveryFailed(
                serviceType: String,
                errorCode: Int
            ) {
                Log.e(TAG, "Discovery failed: Error code: $errorCode")
            }
        })
        nsdHelper.mNsdManager!!.discoverServices(
            nsdHelper.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, NsdHelper.mDiscoveryListener
        )
    }

}