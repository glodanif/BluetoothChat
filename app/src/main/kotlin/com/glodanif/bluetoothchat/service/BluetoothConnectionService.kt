package com.glodanif.bluetoothchat.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.Storage
import com.glodanif.bluetoothchat.database.ChatDatabase
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.entity.Message
import com.glodanif.bluetoothchat.model.OnConnectionListener
import com.glodanif.bluetoothchat.model.OnMessageListener
import com.glodanif.bluetoothchat.model.SettingsManager
import com.glodanif.bluetoothchat.model.SettingsManagerImpl
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.thread

class BluetoothConnectionService : Service() {

    private val TAG = "BCS"

    private val binder = ConnectionBinder()

    enum class ConnectionState { CONNECTED, CONNECTING, NOT_CONNECTED, REJECTED, PENDING, LISTENING }
    enum class ConnectionType { INCOMING, OUTCOMING }

    private var connectionListener: OnConnectionListener? = null
    private var messageListener: OnMessageListener? = null

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val APP_NAME = "BluetoothChat"
    private val APP_UUID = UUID.fromString("220da3b2-41f5-11e7-a919-92ebcb67fe33")

    private val handler: Handler = Handler()
    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private var connectionState: ConnectionState = ConnectionState.NOT_CONNECTED

    private var currentSocket: BluetoothSocket? = null
    private var currentConversation: Conversation? = null

    private val db: ChatDatabase = Storage.getInstance(this).db
    private lateinit var settings: SettingsManager;

    private lateinit var application: ChatApplication
    private lateinit var notificationView: NotificationView

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class ConnectionBinder : Binder() {

        fun getService(): BluetoothConnectionService {
            return this@BluetoothConnectionService
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "CREATED")
        application = getApplication() as ChatApplication
        settings = SettingsManagerImpl(this)
        notificationView = NotificationViewImpl(this)
        isRunning = true
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (intent.action == ACTION_STOP) {

            connectionState = ConnectionState.NOT_CONNECTED
            cancelConnections()
            acceptThread?.cancel()

            connectionListener?.onDisconnected()

            stopSelf()
            return START_NOT_STICKY
        }

        prepareForAccept()
        showNotification("Ready to connect")
        return Service.START_STICKY
    }

    fun getCurrentConversation(): Conversation? {
        return currentConversation
    }

    private fun showNotification(message: String) {
        val notification = notificationView.getForegroundNotification(message)
        startForeground(FOREGROUND_SERVICE, notification)
    }

    @Synchronized fun prepareForAccept() {

        Log.d(TAG, "start")

        cancelConnections()

        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
        showNotification("Ready to connect")
    }

    @Synchronized fun connect(device: BluetoothDevice) {

        Log.d(TAG, "connect to: " + device)

        if (connectionState == ConnectionState.CONNECTING) {
            connectThread?.cancel()
            connectThread = null
        }

        connectedThread?.cancel()
        connectedThread = null
        currentSocket = null
        currentConversation = null

        connectThread = ConnectThread(device)
        connectThread!!.start()
        handler.post { connectionListener?.onConnecting() }
    }

    @Synchronized fun connected(socket: BluetoothSocket, device: BluetoothDevice, type: ConnectionType) {

        Log.d(TAG, "connected")

        cancelConnections()

        currentSocket = socket

        acceptThread?.cancel()
        acceptThread = null

        connectedThread = ConnectedThread(socket, type)
        connectedThread!!.start()
    }

    @Synchronized fun stop() {

        Log.d(TAG, "stop")

        cancelConnections()

        acceptThread?.cancel()
        acceptThread = null

        connectionState = ConnectionState.NOT_CONNECTED
        handler.post { connectionListener?.onDisconnected() }
    }

    private fun cancelConnections() {
        connectThread?.cancel()
        connectThread = null
        connectedThread?.cancel()
        connectedThread = null
        currentSocket = null
        currentConversation = null
    }

    fun sendMessage(message: Message) {

        if (isConnected()) {
            connectedThread?.write(message.getDecodedMessage())
        }

        if (message.type == Message.Type.CONNECTION_RESPONSE) {
            if (message.flag) {
                connectionState = ConnectionState.CONNECTED
            } else {
                prepareForAccept()
            }
            notificationView.dismissConnectionNotification()
        }
    }

    private fun onMessageSent(messageBody: String) {

        if (currentSocket == null) return

        val message = Message(messageBody)

        val sentMessage: ChatMessage = ChatMessage(
                currentSocket!!.remoteDevice.address, Date(), true, message.body)

        if (message.type == Message.Type.MESSAGE) {
            sentMessage.seenHere = true
            thread {
                db.messagesDao().insert(sentMessage)
                handler.post { messageListener?.onMessageSent(sentMessage) }
            }
        }
    }

    private fun onMessageReceived(messageBody: String) {

        Log.e(TAG, "Message received: $messageBody")

        val message = Message(messageBody)

        if (message.type == Message.Type.MESSAGE && currentSocket != null) {

            val device: BluetoothDevice = currentSocket!!.remoteDevice

            val receivedMessage: ChatMessage =
                    ChatMessage(device.address, Date(), false, message.body)

            if (messageListener == null || application.currentChat == null || !application.currentChat.equals(device.address)) {
                notificationView.showNewMessageNotification(message.body, device.name, device.address)
            } else {
                receivedMessage.seenHere = true
            }
            thread {
                db.messagesDao().insert(receivedMessage)
                handler.post { messageListener?.onMessageReceived(receivedMessage) }
            }

        } else if (message.type == Message.Type.DELIVERY) {
            if (message.flag) {
                messageListener?.onMessageDelivered(message.uid)
            } else {
                messageListener?.onMessageNotDelivered(message.uid)
            }
        } else if (message.type == Message.Type.SEEING) {
            if (message.flag) {
                messageListener?.onMessageSeen(message.uid)
            }
        } else if (message.type == Message.Type.CONNECTION_RESPONSE) {
            if (message.flag) {

                val device: BluetoothDevice = currentSocket!!.remoteDevice

                val parts = message.body.split("#")
                val conversation = Conversation(device.address, device.name, parts[0], parts[1].toInt())
                thread { db.conversationsDao().insert(conversation) }

                currentConversation = conversation

                connectionState = ConnectionState.CONNECTED
                connectionListener?.onConnectionAccepted()
                connectionListener?.onConnectedOut(conversation)

            } else {
                connectionState = ConnectionState.REJECTED
                prepareForAccept()
                connectionListener?.onConnectionRejected()
            }
        } else if (message.type == Message.Type.CONNECTION_REQUEST && currentSocket != null) {

            val device: BluetoothDevice = currentSocket!!.remoteDevice

            val parts = message.body.split("#")
            val conversation = Conversation(device.address, device.name, parts[0], parts[1].toInt())
            thread { db.conversationsDao().insert(conversation) }

            currentConversation = conversation

            connectionListener?.onConnectedIn(conversation)

            if (!application.isConversationsOpened && !(application.currentChat != null && application.currentChat.equals(device.address))) {
                notificationView.showConnectionRequestNotification(
                        "${conversation.displayName} (${conversation.deviceName})")
            }
        }
    }

    private fun connectionFailed() {
        currentSocket = null
        currentConversation = null
        handler.post { connectionListener?.onConnectionFailed() }
        connectionState = ConnectionState.NOT_CONNECTED
        prepareForAccept()
    }

    private fun connectionLost() {
        currentSocket = null
        currentConversation = null
        if (isConnected()) {
            handler.post { connectionListener?.onConnectionLost() }
        }
        connectionState = ConnectionState.NOT_CONNECTED
        prepareForAccept()
    }

    fun isConnected(): Boolean {
        return connectionState == ConnectionState.CONNECTED ||
                connectionState == ConnectionState.PENDING
    }

    fun isPending(): Boolean {
        return connectionState == ConnectionState.PENDING
    }

    fun setConnectionListener(listener: OnConnectionListener?) {
        this.connectionListener = listener
    }

    fun setMessageListener(listener: OnMessageListener?) {
        this.messageListener = listener
    }

    private inner class AcceptThread : Thread() {

        private var serverSocket: BluetoothServerSocket? = null

        init {
            try {
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket listen() failed", e)
                e.printStackTrace()
            }

            connectionState = ConnectionState.LISTENING
        }

        override fun run() {

            Log.d(TAG, "BEGIN acceptThread" + this)

            var socket: BluetoothSocket?

            while (!isConnected()) {
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket accept() failed")
                    break
                }

                if (socket != null) {
                    when (connectionState) {
                        ConnectionState.LISTENING, ConnectionState.CONNECTING -> {
                            Log.e(TAG, "AcceptThread")
                            connected(socket, socket.remoteDevice, ConnectionType.INCOMING)
                        }
                        ConnectionState.NOT_CONNECTED, ConnectionState.CONNECTED, ConnectionState.PENDING, ConnectionState.REJECTED -> try {
                            socket.close()
                        } catch (e: IOException) {
                            Log.e(TAG, "Could not close unwanted socket", e)
                        }
                    }
                }
            }

            Log.i(TAG, "END acceptThread")
        }

        fun cancel() {
            Log.d(TAG, "Socket cancel " + this)
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Socket close() of server failed", e)
                e.printStackTrace()
            }
        }
    }

    private inner class ConnectThread(bluetoothDevice: BluetoothDevice) : Thread() {

        private var socket: BluetoothSocket? = null
        private val device = bluetoothDevice

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(APP_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket create() failed", e)
                e.printStackTrace()
            }
            connectionState = ConnectionState.CONNECTING
        }

        override fun run() {

            Log.i(TAG, "BEGIN connectThread")

            try {
                socket?.connect()
            } catch (connectException: IOException) {
                connectException.printStackTrace()
                try {
                    socket?.close()
                } catch (closeException: IOException) {
                    closeException.printStackTrace()
                    Log.e(TAG, "unable to close() socket during connection failure", closeException)
                }
                connectionFailed()
                return
            }

            synchronized(this@BluetoothConnectionService) {
                connectThread = null
            }

            if (socket != null) {
                Log.e(TAG, "ConnectThread")
                connected(socket!!, device, ConnectionType.OUTCOMING)
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket, type: ConnectionType) : Thread() {

        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null

        init {
            Log.d(TAG, "create ConnectedThread, connected:${socket.isConnected}")

            try {
                inputStream = socket.inputStream
                outputStream = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "sockets not created", e)
            }

            showNotification("Connected to ${socket.remoteDevice.name}")
            connectionState = ConnectionState.PENDING
            if (type == ConnectionType.OUTCOMING) {
                val message = Message.createConnectMessage(settings.getUserName(), settings.getUserColor())
                write(message.getDecodedMessage())
            }
        }

        override fun run() {
            Log.i(TAG, "BEGIN connectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int?

            while (isConnected()) {
                try {
                    bytes = inputStream?.read(buffer)

                    if (bytes != null) {
                        handler.post { onMessageReceived(String(buffer, 0, bytes!!)) }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        fun write(message: String) {
            try {
                outputStream?.write(message.toByteArray(Charsets.UTF_8))
                onMessageSent(message)
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                socket.close()
                currentSocket = null
                currentConversation = null
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        currentSocket = null
        currentConversation = null
        Log.e(TAG, "DESTROYED")
    }

    companion object {

        var isRunning = false

        var FOREGROUND_SERVICE = 101
        var ACTION_STOP = "action.stop"

        fun start(context: Context) {
            val intent = Intent(context, BluetoothConnectionService::class.java)
            context.startService(intent)
        }

        fun bind(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, BluetoothConnectionService::class.java)
            context.bindService(intent, connection, AppCompatActivity.BIND_ABOVE_CLIENT)
        }
    }
}
