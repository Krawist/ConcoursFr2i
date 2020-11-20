package cm.seeds.concoursfr2i.Fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import cm.seeds.concoursfr2i.Helper.REQUEST_CODE_FOR_ACCESS_LOCATION_PERMISSION
import cm.seeds.concoursfr2i.Helper.showToast
import cm.seeds.concoursfr2i.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.net.ServerSocket
import kotlin.properties.Delegates


class ShareFragment : Fragment() {

    private val peers = mutableListOf<WifiP2pDevice>()

    private var fabSend by Delegates.notNull<FloatingActionButton>()
    private var fabReceive by Delegates.notNull<FloatingActionButton>()
    private var layoutFoundedDevice by Delegates.notNull<LinearLayout>()

    private var manager: WifiP2pManager? = null

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)
            if(peers.isNotEmpty()){
                for (device in peers) {
                    addDeviceToFoundedDeviceView(device)
                }
            }else{
                addDeviceToFoundedDeviceView(null)
            }
        }
        if (peers.isEmpty()) {
            addDeviceToFoundedDeviceView(null)
        }
    }

    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->

        // String from WifiP2pInfo struct
        val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress

        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }
    }

    private var connectedDevice : WifiP2pDevice? = null

    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initConnectionSettings()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode== REQUEST_CODE_FOR_ACCESS_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share_fragmentt, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        receiver?.also { receiver ->
            requireContext().unregisterReceiver(receiver)
        }
    }

    override fun onResume() {
        super.onResume()
        receiver?.also {
            requireActivity().registerReceiver(receiver, intentFilter)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseViews(view)

        addActionsonViews()
    }

    private fun addActionsonViews() {
        fabSend.setOnClickListener {

        }

        fabReceive.setOnClickListener {
            checkAndConnectToPeers()
        }
    }

    private fun initConnectionSettings() {
        manager = requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager?.initialize(requireContext(), Looper.getMainLooper(), null)
        receiver = WiFiDirectBroadcastReceiver(this)
    }

    private fun checkAndConnectToPeers() {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                showToast(requireActivity(),"Discovery start...")
            }

            override fun onFailure(reasonCode: Int) {
                showToast(requireActivity(),"Discovery fail $reasonCode")
            }
        })
    }

    private fun initialiseViews(view: View) {
        fabReceive = view.findViewById(R.id.fab_receive)
        fabSend = view.findViewById(R.id.fab_send)
        layoutFoundedDevice = view.findViewById(R.id.layout_founded_device)
    }

    private fun addDeviceToFoundedDeviceView(device: WifiP2pDevice?) {
        val view = LayoutInflater.from(layoutFoundedDevice.context).inflate(android.R.layout.simple_list_item_2, layoutFoundedDevice, false)
        if(device!=null){
            view.findViewById<TextView>(android.R.id.text1).text = (device.deviceName)
            view.findViewById<TextView>(android.R.id.text2).text = (getDeviceStatus(device.status))

            view.setOnClickListener {
                connectToDevice(device)
            }
        }else{
            view.findViewById<TextView>(android.R.id.text1).text = "Aucun Device trouvé"
        }

        layoutFoundedDevice.addView(view)
    }

    private fun getDeviceStatus(deviceStatus: Int): String {
        return when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
            }

            override fun onFailure(reason: Int) {
                showToast(requireActivity(),"Echec de la connection au device ${device.deviceName}")
            }
        })
    }

    private fun handleWifiStateChanged(state : Int) {
        when (state) {
            WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                showToast(requireActivity(),"WIFI P2P is enabled")
                checkAndConnectToPeers()
            }
            else -> {
                showToast(requireActivity(),"WIFI P2P is not enabled")
            }
        }
    }


    inner class WiFiDirectBroadcastReceiver(private val fragment: ShareFragment) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    fragment.handleWifiStateChanged(intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1))
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {

                    // The peer list has changed! We should probably do something about
                    // that.
                    manager?.requestPeers(channel, peerListListener)

                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {

                    // Connection state changed! We should probably do something about
                    // that.
                    manager?.let { manager ->

                        val networkInfo: NetworkInfo? = intent
                            .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo

                        if (networkInfo?.isConnected == true) {

                            // We are connected with the other device, request connection
                            // info to find group owner IP

                            manager.requestConnectionInfo(channel, connectionListener)
                        }
                    }

                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

                }
            }
        }
    }


    inner class FileServerAsyncTask(private val context: Context, private var statusText: TextView) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg params: Void): String? {
            /**
             * Create a server socket.
             */
            val serverSocket = ServerSocket(8888)
            return serverSocket.use {
                /**
                 * Wait for client connections. This call blocks until a
                 * connection is accepted from a client.
                 */
                val client = serverSocket.accept()
                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                val f = File("${requireContext().getExternalFilesDir(null)}/${System.currentTimeMillis()}.jpg")
                val dirs = File(f.parent)

                dirs.takeIf { it.doesNotExist() }?.apply {
                    mkdirs()
                }
                f.createNewFile()
                val inputstream = client.getInputStream()
                //copyFile(inputstream, FileOutputStream(f))
                serverSocket.close()
                f.absolutePath
            }
        }

        private fun File.doesNotExist(): Boolean = !exists()

        /**
         * Start activity that can handle the JPEG image
         */
        override fun onPostExecute(result: String?) {
            result?.run {
                statusText.text = "File copied - $result"
                val intent = Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse("file://$result"), "image/*")
                }
                context.startActivity(intent)
            }
        }
    }
}