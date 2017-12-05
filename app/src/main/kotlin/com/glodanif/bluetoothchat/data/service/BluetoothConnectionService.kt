package com.glodanif.bluetoothchat.data.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.glodanif.bluetoothchat.BuildConfig
import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.database.Storage
import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.entity.Message
import com.glodanif.bluetoothchat.data.entity.MessageType
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.view.NotificationViewImpl
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
import java.io.*
import java.util.*
import kotlin.concurrent.thread
import android.graphics.BitmapFactory


class BluetoothConnectionService : Service() {

    private val TAG = "BCS"

    private val binder = ConnectionBinder()

    enum class ConnectionState { CONNECTED, CONNECTING, NOT_CONNECTED, REJECTED, PENDING, LISTENING }
    enum class ConnectionType { INCOMING, OUTCOMING }

    private var connectionListener: OnConnectionListener? = null
    private var messageListener: OnMessageListener? = null
    private var fileListener: OnFileListener? = null

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val APP_NAME = "BluetoothChat"
    private val APP_UUID = UUID.fromString("220da3b2-41f5-11e7-a919-92ebcb67fe33")

    private val handler: Handler = Handler()

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var dataTransferThread: DataTransferThread? = null

    @Volatile
    private var connectionState: ConnectionState = ConnectionState.NOT_CONNECTED
    @Volatile
    private var connectionType: ConnectionType? = null

    private var currentSocket: BluetoothSocket? = null
    private var currentConversation: Conversation? = null

    private val db: ChatDatabase = Storage.getInstance(this).db
    private lateinit var preferences: Preferences
    private lateinit var settings: SettingsManager

    private lateinit var application: ChatApplication
    private lateinit var notificationView: NotificationView
    private lateinit var shortcutManager: ShortcutManager

    override fun onBind(intent: Intent?): IBinder? {
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
        preferences = UserPreferences(this)
        notificationView = NotificationViewImpl(this)
        shortcutManager = ShortcutManagerImpl(this)
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_STOP) {

            connectionState = ConnectionState.NOT_CONNECTED
            cancelConnections()
            acceptThread?.cancel()
            acceptThread = null

            connectionListener?.onConnectionDestroyed()

            stopSelf()
            return START_NOT_STICKY
        }

        prepareForAccept()
        showNotification(getString(R.string.notification__ready_to_connect))
        return Service.START_STICKY
    }

    fun getCurrentConversation(): Conversation? {
        return currentConversation
    }

    private fun showNotification(message: String) {
        val notification = notificationView.getForegroundNotification(message)
        startForeground(FOREGROUND_SERVICE, notification)
    }

    @Synchronized
    fun disconnect() {
        dataTransferThread?.cancel(true)
        dataTransferThread = null
        prepareForAccept()
    }

    @Synchronized
    fun prepareForAccept() {

        Log.d(TAG, "start")

        cancelConnections()

        acceptThread = AcceptThread()
        acceptThread!!.start()
        showNotification(getString(R.string.notification__ready_to_connect))
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {

        log("connect to: ${device.name}")

        if (connectionState == ConnectionState.CONNECTING) {
            connectThread?.cancel()
            connectThread = null
        }

        dataTransferThread?.cancel(true)
        acceptThread?.cancel()
        dataTransferThread = null
        acceptThread = null
        currentSocket = null
        currentConversation = null
        connectionType = null

        connectThread = ConnectThread(device)
        connectThread!!.start()
        handler.post { connectionListener?.onConnecting() }
    }

    @Synchronized
    fun connected(socket: BluetoothSocket, type: ConnectionType) {

        cancelConnections()

        connectionType = type

        currentSocket = socket

        acceptThread?.cancel()
        acceptThread = null

        val transferEventsListener = object : DataTransferThread.TransferEventsListener {

            override fun onMessageReceived(message: String) {
                handler.post { this@BluetoothConnectionService.onMessageReceived(message) }
            }

            override fun onMessageSent(message: String) {
                this@BluetoothConnectionService.onMessageSent(message)
            }

            override fun onConnectionPrepared(type: ConnectionType) {

                showNotification("Connected to ${socket.remoteDevice.name}")
                connectionState = ConnectionState.PENDING

                if (type == ConnectionType.OUTCOMING) {
                    val message = Message.createConnectMessage(settings.getUserName(), settings.getUserColor())
                    dataTransferThread?.write(message.getDecodedMessage())
                }
            }

            override fun onConnectionCanceled() {
                currentSocket = null
                currentConversation = null
            }

            override fun onConnectionLost() {
                connectionLost()
            }
        }

        val fileEventsListener = object : DataTransferThread.OnFileListener {

            override fun onFileSendingStarted(file: File) {
                fileListener?.onFileSendingStarted(file.absolutePath, file.length())
            }

            override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {
                handler.post { fileListener?.onFileSendingProgress(sentBytes, totalBytes) }
            }

            override fun onFileSendingFinished(filePath: String) {

                val endMessage = Message.createFileEndMessage()
                dataTransferThread?.write(endMessage.getDecodedMessage())

                if (currentSocket == null) return

                val sentMessage = ChatMessage(currentSocket!!.remoteDevice.address, Date(), true, "")

                sentMessage.seenHere = true
                sentMessage.messageType = MessageType.IMAGE
                sentMessage.filePath = filePath

                thread {

                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true

                    BitmapFactory.decodeFile(filePath, options)
                    val width = options.outWidth
                    val height = options.outHeight
                    sentMessage.fileInfo = "${width}x$height"

                    db.messagesDao().insert(sentMessage)

                    handler.post {
                        fileListener?.onFileSendingFinished()
                        messageListener?.onMessageSent(sentMessage)
                    }

                    if (currentConversation != null) {
                        shortcutManager.addConversationShortcut(sentMessage.deviceAddress,
                                currentConversation!!.displayName, currentConversation!!.color)
                    }
                }
            }

            override fun onFileSendingCanceled() {
                fileListener?.onFileReceivingCanceled()
            }

            override fun onFileSendingFailed() {
                fileListener?.onFileSendingFailed()
            }

            override fun onFileReceivingStarted(fileSize: Long) {
                handler.post { fileListener?.onFileReceivingStarted(fileSize) }
            }

            override fun onFileReceivingProgress(receivedBytes: Long, totalBytes: Long) {
                handler.post { fileListener?.onFileReceivingProgress(receivedBytes, totalBytes) }
            }

            override fun onFileReceivingFinished(filePath: String) {

                val device: BluetoothDevice = currentSocket!!.remoteDevice

                val receivedMessage = ChatMessage(device.address, Date(), false, "")
                receivedMessage.messageType = MessageType.IMAGE
                receivedMessage.filePath = filePath

                if (messageListener == null || application.currentChat == null || !application.currentChat.equals(device.address)) {
                    notificationView.showNewMessageNotification("Picture", currentConversation?.displayName,
                            device.name, device.address, preferences.getSettings())
                } else {
                    receivedMessage.seenHere = true
                }

                thread {

                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true

                    BitmapFactory.decodeFile(filePath, options)
                    val width = options.outWidth
                    val height = options.outHeight
                    receivedMessage.fileInfo = "${width}x$height"

                    db.messagesDao().insert(receivedMessage)

                    handler.post {
                        fileListener?.onFileReceivingFinished()
                        messageListener?.onMessageReceived(receivedMessage)
                    }
                    if (currentConversation != null) {
                        shortcutManager.addConversationShortcut(device.address,
                                currentConversation!!.displayName, currentConversation!!.color)
                    }
                }
            }

            override fun onFileReceivingCanceled() {
                fileListener?.onFileReceivingCanceled()
            }

            override fun onFileReceivingFailed() {
                fileListener?.onFileReceivingFailed()
            }

        }

        dataTransferThread = object : DataTransferThread(this, socket, type, transferEventsListener, fileEventsListener) {

            override fun shouldRun(): Boolean {
                return isConnectedOrPending()
            }
        }
        dataTransferThread?.prepare()
        dataTransferThread?.start()

        handler.post { connectionListener?.onConnected(socket.remoteDevice) }

        log("connected")
    }

    @Synchronized
    fun stop() {

        cancelConnections()

        acceptThread?.cancel()
        acceptThread = null

        connectionState = ConnectionState.NOT_CONNECTED

        log("stop")
    }

    private fun cancelConnections() {
        log("cancelConnections")
        connectThread?.cancel()
        connectThread = null
        dataTransferThread?.cancel()
        dataTransferThread = null
        currentSocket = null
        currentConversation = null
        connectionType = null
    }

    fun sendMessage(message: Message) {

        if (isConnectedOrPending()) {

            val disconnect = message.type == Message.Type.CONNECTION_REQUEST && !message.flag

            dataTransferThread?.write(message.getDecodedMessage(), disconnect)

            if (disconnect) {
                dataTransferThread?.cancel(disconnect)
                dataTransferThread = null

                prepareForAccept()
            }
        }

        if (message.type == Message.Type.CONNECTION_RESPONSE) {
            if (message.flag) {
                connectionState = ConnectionState.CONNECTED
            } else {
                disconnect()
            }
            notificationView.dismissConnectionNotification()
        }
    }

    fun sendFile(file: File, type: MessageType) {

        if (!isConnected()) {
            return
        }

        val startMessage = Message.createFileStartMessage(file, MessageType.IMAGE)

        dataTransferThread?.write(startMessage.getDecodedMessage())
        dataTransferThread?.writeFile(file)
    }

    private fun onMessageSent(messageBody: String) {

        if (currentSocket == null) return

        val message = Message(messageBody)

        val sentMessage = ChatMessage(currentSocket!!.remoteDevice.address, Date(), true, message.body)

        if (message.type == Message.Type.MESSAGE) {
            sentMessage.seenHere = true
            thread {
                db.messagesDao().insert(sentMessage)
                handler.post { messageListener?.onMessageSent(sentMessage) }
                if (currentConversation != null) {
                    shortcutManager.addConversationShortcut(sentMessage.deviceAddress,
                            currentConversation!!.displayName, currentConversation!!.color)
                }
            }
        }
    }

    private fun onMessageReceived(messageBody: String) {

        log("Message received: $messageBody")

        val message = Message(messageBody)

        if (message.type == Message.Type.MESSAGE && currentSocket != null) {

            handleReceivedMessage(message.body)

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
                handleConnectionApproval(message)
            } else {
                connectionState = ConnectionState.REJECTED
                prepareForAccept()
                connectionListener?.onConnectionRejected()
            }
        } else if (message.type == Message.Type.CONNECTION_REQUEST && currentSocket != null) {

            if (message.flag) {
                handleConnectionRequest(message)
            } else {
                disconnect()
                connectionListener?.onDisconnected()
            }
        } else if (message.type == Message.Type.FILE_START) {

            dataTransferThread?.isFileLoading = true
            dataTransferThread?.fileName = message.body.substringBefore("#")

        } else if (message.type == Message.Type.FILE_END) {

            dataTransferThread?.isFileLoading = false
            dataTransferThread?.fileName = null

        }
    }

    private fun handleReceivedMessage(text: String) {

        val device: BluetoothDevice = currentSocket!!.remoteDevice

        val receivedMessage = ChatMessage(device.address, Date(), false, text)

        if (messageListener == null || application.currentChat == null || !application.currentChat.equals(device.address)) {
            notificationView.showNewMessageNotification(text, currentConversation?.displayName,
                    device.name, device.address, preferences.getSettings())
        } else {
            receivedMessage.seenHere = true
        }
        thread {
            db.messagesDao().insert(receivedMessage)
            handler.post { messageListener?.onMessageReceived(receivedMessage) }
            if (currentConversation != null) {
                shortcutManager.addConversationShortcut(device.address,
                        currentConversation!!.displayName, currentConversation!!.color)
            }
        }
    }

    private fun handleConnectionRequest(message: Message) {

        val device: BluetoothDevice = currentSocket!!.remoteDevice

        val parts = message.body.split("#")
        val conversation = Conversation(device.address, device.name, parts[0], parts[1].toInt())

        val messageContractVersion = if (parts.size >= 3) parts[2].toInt() else 0
        conversation.messageContractVersion = messageContractVersion

        thread { db.conversationsDao().insert(conversation) }

        currentConversation = conversation

        connectionListener?.onConnectedIn(conversation)

        if (!application.isConversationsOpened && !(application.currentChat != null && application.currentChat.equals(device.address))) {
            notificationView.showConnectionRequestNotification(
                    "${conversation.displayName} (${conversation.deviceName})", preferences.getSettings())
        }
    }

    private fun handleConnectionApproval(message: Message) {

        val device: BluetoothDevice = currentSocket!!.remoteDevice

        val parts = message.body.split("#")
        val conversation = Conversation(device.address, device.name, parts[0], parts[1].toInt())

        val messageContractVersion = if (parts.size >= 3) parts[2].toInt() else 0
        conversation.messageContractVersion = messageContractVersion

        thread { db.conversationsDao().insert(conversation) }

        currentConversation = conversation

        connectionState = ConnectionState.CONNECTED
        connectionListener?.onConnectionAccepted()
        connectionListener?.onConnectedOut(conversation)
    }

    private fun connectionFailed() {
        log("connectionFailed")
        currentSocket = null
        currentConversation = null
        handler.post { connectionListener?.onConnectionFailed() }
        connectionState = ConnectionState.NOT_CONNECTED
        prepareForAccept()
    }

    private fun connectionLost() {

        log("connectionLost")

        currentSocket = null
        currentConversation = null
        if (isConnectedOrPending()) {
            handler.post {
                if (isPending() && connectionType == ConnectionType.INCOMING) {
                    connectionState = ConnectionState.NOT_CONNECTED
                    connectionListener?.onConnectionWithdrawn()
                    log("onConnectionWithdrawn")
                } else {
                    connectionState = ConnectionState.NOT_CONNECTED
                    connectionListener?.onConnectionLost()
                    log("onConnectionLost")
                }
                log("connectionLost - connected - prepareForAccept")
                prepareForAccept()
            }
        } else {
            log("connectionLost - not connected - prepareForAccept")
            prepareForAccept()
        }
    }

    fun isConnected(): Boolean {
        return connectionState == ConnectionState.CONNECTED
    }

    fun isConnectedOrPending(): Boolean {
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

    fun setFileListener(listener: OnFileListener?) {
        this.fileListener = listener
    }

    private inner class AcceptThread : Thread() {

        private var serverSocket: BluetoothServerSocket? = null

        init {
            try {
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
            } catch (e: IOException) {
                log("Socket listen() failed: ${e.message}")
                e.printStackTrace()
            }

            connectionState = ConnectionState.LISTENING
        }

        override fun run() {

            log("BEGIN acceptThread")

            var socket: BluetoothSocket?

            while (!isConnectedOrPending()) {
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    log("Socket accept() failed")
                    break
                }

                if (socket != null) {
                    when (connectionState) {
                        ConnectionState.LISTENING, ConnectionState.CONNECTING -> {
                            log("Connected in AcceptThread")
                            connected(socket, ConnectionType.INCOMING)
                        }
                        ConnectionState.NOT_CONNECTED, ConnectionState.CONNECTED, ConnectionState.PENDING, ConnectionState.REJECTED -> try {
                            socket.close()
                        } catch (e: IOException) {
                            log("Could not close unwanted socket: ${e.message}")
                        }
                    }
                }
            }

            log("END acceptThread")
        }

        fun cancel() {
            log("cancel() AcceptThread")
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                log("cancel() AcceptThread failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private inner class ConnectThread(private val bluetoothDevice: BluetoothDevice) : Thread() {

        private var socket: BluetoothSocket? = null

        override fun run() {

            log("BEGIN connectThread")

            try {
                socket = bluetoothDevice.createRfcommSocketToServiceRecord(APP_UUID)
            } catch (e: IOException) {
                log("unable to create socket in ConnectThread: ${e.message}")
                e.printStackTrace()
            }
            connectionState = ConnectionState.CONNECTING

            try {
                socket?.connect()
            } catch (connectException: IOException) {
                connectException.printStackTrace()
                try {
                    socket?.close()
                } catch (e: IOException) {
                    log("unable to close() socket during connection failure: ${e.message}")
                }
                connectionFailed()
                return
            }

            synchronized(this@BluetoothConnectionService) {
                connectThread = null
            }

            if (socket != null) {
                log("connect thread - connected")
                connected(socket!!, ConnectionType.OUTCOMING)
            }

            log("END connectThread")
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                log("cancel() of connect thread failed: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        cancelConnections()
        acceptThread?.cancel()
        acceptThread = null
    }

    companion object {

        var isRunning = false

        var FOREGROUND_SERVICE = 101
        var ACTION_STOP = "action.stop"

        fun start(context: Context) {
            val intent = Intent(context, BluetoothConnectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            context.startService(intent)
        }

        fun bind(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, BluetoothConnectionService::class.java)
            context.bindService(intent, connection, AppCompatActivity.BIND_ABOVE_CLIENT)
        }
    }

    private fun log(text: String) {
        if (BuildConfig.DEBUG) {
            val logBody = "$text - (State: ${connectionState.name}, Type: ${connectionType?.name}, Conversation: $currentConversation, Threads: A: $acceptThread (running: ${acceptThread?.isAlive}), C: $connectThread (running: ${connectThread?.isAlive}), CD: $dataTransferThread (running: ${dataTransferThread?.isAlive}))"
            Log.e(TAG, logBody)
        }
    }
}
