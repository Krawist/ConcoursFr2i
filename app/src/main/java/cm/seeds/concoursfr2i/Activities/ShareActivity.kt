package cm.seeds.concoursfr2i.Activities

import android.app.AlarmManager
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
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cm.seeds.concoursfr2i.AppViewModel
import cm.seeds.concoursfr2i.Fragments.ShareFragment
import cm.seeds.concoursfr2i.Helper.REQUEST_CODE_FOR_ACCESS_LOCATION_PERMISSION
import cm.seeds.concoursfr2i.Helper.showToast
import cm.seeds.concoursfr2i.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.properties.Delegates

class ShareActivity : AppCompatActivity() {

    companion object{
        public const val ACTION_SHARE_CONTENT = "partager_du_contenu"
        public const val ACTION_PREPARE_SHARING = "creer_la_connection"
    }

    private val CONNEXION_PORT = 8008

    private var connectedDevice : WifiP2pDevice? = null

    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null
    private var model : AppViewModel? = null

    private val peers = mutableListOf<WifiP2pDevice>()

    private var fabSend by Delegates.notNull<FloatingActionButton>()
    private var fabReceive by Delegates.notNull<FloatingActionButton>()
    private var layoutFoundedDevice by Delegates.notNull<LinearLayout>()

    //private var serverSocket : ServerSocket? = null
    private var clientSocket : Socket? = null

    private var clients = mutableListOf<Socket>()

    private var manager: WifiP2pManager? = null
    private var pathOfFileToShare: String? = null

    private var connectionEstablished = false

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        layoutFoundedDevice.removeAllViews()
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
            layoutFoundedDevice.removeAllViews()
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

            /// on peut demarrer le serveur ici
            ServerAsyncTask().execute()
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.

            //on peut demarrer le cleint ici
            ClientAsyncTask(info.groupOwnerAddress).execute()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_share_fragmentt)

        initialiseViews()

        addActionsonViews()

        initConnectionSettings()

        model = AppViewModel.getInstance(application)
        clients = model?.listOfConnectedDevice!!
        if(intent!=null){
            when(intent.action){

                ACTION_SHARE_CONTENT ->{
                    pathOfFileToShare = intent.getStringExtra(Intent.EXTRA_STREAM)
                }

            }
        }

    }

    private fun shareContent(
        listOfConnectedDevice: MutableList<Socket>?,
        serverSocket: ServerSocket?,
        file : File
    ) {
        if(serverSocket!=null && true == listOfConnectedDevice?.isNotEmpty()){
            for(clientSOcket in listOfConnectedDevice){
                showToast(this,"Envoi du fichier en cours")
                FileInputStream(file).copyTo(clientSOcket.getOutputStream())
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode== REQUEST_CODE_FOR_ACCESS_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        receiver?.also { receiver ->
            unregisterReceiver(receiver)
        }
        model?.serverSocket?.close()
        model?.listOfConnectedDevice?.forEach {
            it.close()
        }
    }

    override fun onResume() {
        super.onResume()
        receiver?.also {
            registerReceiver(receiver, intentFilter)
        }

    }

    private fun addActionsonViews() {
        fabSend.setOnClickListener {

        }

        fabReceive.setOnClickListener {
            checkAndConnectToPeers()
        }
    }

    private fun initConnectionSettings() {
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager?.initialize(this, Looper.getMainLooper(), null)
        receiver = WiFiDirectBroadcastReceiver()
    }

    private fun checkAndConnectToPeers() {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                showToast(this@ShareActivity,"Discovery start...")
            }

            override fun onFailure(reasonCode: Int) {
                showToast(this@ShareActivity,"Discovery fail $reasonCode")
            }
        })
    }

    private fun initialiseViews() {
        fabReceive = findViewById(R.id.fab_receive)
        fabSend = findViewById(R.id.fab_send)
        layoutFoundedDevice = findViewById(R.id.layout_founded_device)
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
                showToast(this@ShareActivity,"Echec de la connection au device ${device.deviceName}")
            }
        })
    }

    private fun handleWifiStateChanged(state : Int) {
        when (state) {
            WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                showToast(this@ShareActivity,"WIFI P2P is enabled")
                checkAndConnectToPeers()
            }
            else -> {
                showToast(this,"WIFI P2P is not enabled")
            }
        }
    }


    inner class WiFiDirectBroadcastReceiver() : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    handleWifiStateChanged(intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1))
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
                    val networkInfo = intent.getParcelableArrayExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo?
                    if(true==networkInfo?.isConnected){
                        connectionEstablished = true
                        manager?.requestConnectionInfo(channel,connectionListener)
                    }else{
                        connectionEstablished = false
                    }
                }
            }
        }
    }

    private fun sendFile(file: File){
        if(model?.serverSocket==null){
            model?.serverSocket = ServerSocket(CONNEXION_PORT)
        }

        if(clients.isEmpty()){
            showToast(this,"Attente de client")
        }else{
            clients.forEach {
                copyFile(FileOutputStream(file),it.getInputStream())
            }
        }
    }

    private fun copyFile(fileOutputStream: FileOutputStream, inputStream: InputStream?) {
        TODO("Not yet implemented")
    }


    inner class ServerAsyncTask() : AsyncTask<Any, Any, Any>(){
        override fun doInBackground(vararg params: Any?): Any {
            model?.serverSocket = ServerSocket(CONNEXION_PORT)
            while (connectionEstablished){
                val newClient = model?.serverSocket?.accept()
                publishProgress("Connection d'un nouveau client")
                if(newClient!=null){
                    var previousInstance : Socket? = null
                    if(clients.isNotEmpty()){
                        clients.forEach {
                            if(newClient.inetAddress == it.inetAddress){
                                previousInstance  = it
                            }
                        }

                        if(previousInstance!=null){
                            clients.remove(previousInstance!!)
                        }
                    }
                    clients.add(newClient)
                    if(pathOfFileToShare!=null){
                        shareContent(clients,model?.serverSocket,File(pathOfFileToShare))
                    }
                }
            }

            return Any()
        }

        override fun onProgressUpdate(vararg values: Any?) {
            if(values[0] is String){
                showToast(this@ShareActivity,values[0] as String)
            }
            super.onProgressUpdate(*values)
        }
    }

    inner class ClientAsyncTask(val hostAdress : InetAddress) : AsyncTask<Any, Any, Any>(){
        override fun doInBackground(vararg params: Any?): Any {
            val clientSocket = Socket()
            clientSocket.connect(InetSocketAddress(hostAdress,CONNEXION_PORT))
            publishProgress("connecté à l'hote")
            while (connectionEstablished){
               val stream =  clientSocket.getInputStream()
                val fichierRecu = File(getExternalFilesDir(null),"received.jpg")
                stream.copyTo(FileOutputStream(fichierRecu))
                publishProgress("Recption d'un contenu de l'hote")
            }

            return Any()
        }

        override fun onProgressUpdate(vararg values: Any?) {
            if(values[0] is String){
                showToast(this@ShareActivity,values[0] as String)
            }
            super.onProgressUpdate(*values)
        }
    }

}