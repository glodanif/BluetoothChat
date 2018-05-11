package com.glodanif.bluetoothchat.data.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.database.Storage
import com.glodanif.bluetoothchat.data.entity.*
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.view.NotificationViewImpl
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
import com.glodanif.bluetoothchat.utils.Size
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
import java.util.*

class BluetoothConnectionService : Service() {

    private val binder = ConnectionBinder()
    private val uiContext = UI
    private val bgContext = CommonPool
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val APP_NAME = "BluetoothChat"
    private val APP_UUID = UUID.fromString("220da3b2-41f5-11e7-a919-92ebcb67fe33")

    enum class ConnectionState { CONNECTED, CONNECTING, NOT_CONNECTED, REJECTED, PENDING, LISTENING }
    enum class ConnectionType { INCOMING, OUTCOMING }

    private var connectionListener: OnConnectionListener? = null
    private var messageListener: OnMessageListener? = null
    private var fileListener: OnFileListener? = null

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
    private lateinit var preferences: UserPreferences
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
        application = getApplication() as ChatApplication
        settings = SettingsManagerImpl(this)
        preferences = UserPreferencesImpl(this)
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

    fun getCurrentConversation() = currentConversation

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
    private fun prepareForAccept() {
        cancelConnections()
        acceptThread = AcceptThread()
        acceptThread?.start()
        showNotification(getString(R.string.notification__ready_to_connect))
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {

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
        connectThread?.start()

        launch(uiContext) { connectionListener?.onConnecting() }
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
                launch(uiContext) { this@BluetoothConnectionService.onMessageReceived(message) }
            }

            override fun onMessageSent(message: String) {
                launch(uiContext) { this@BluetoothConnectionService.onMessageSent(message) }
            }

            override fun onConnectionPrepared(type: ConnectionType) {

                showNotification(getString(R.string.notification__connected_to, socket.remoteDevice.name ?: "?"))
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

            override fun onFileSendingStarted(file: TransferringFile) {

                fileListener?.onFileSendingStarted(file.name, file.size)

                currentConversation?.let {

                    val silently = application.currentChat != null && currentSocket != null &&
                            application.currentChat.equals(currentSocket?.remoteDevice?.address)

                    notificationView.showFileTransferNotification(it.displayName, it.deviceName,
                            it.deviceAddress, file, 0, silently, preferences.getSettings())
                }
            }

            override fun onFileSendingProgress(file: TransferringFile, sentBytes: Long) {

                launch(uiContext) {

                    fileListener?.onFileSendingProgress(sentBytes, file.size)

                    if (currentConversation != null) {
                        notificationView.updateFileTransferNotification(sentBytes, file.size)
                    }
                }
            }

            override fun onFileSendingFinished(path: String) {

                val endMessage = Message.createFileEndMessage()
                dataTransferThread?.write(endMessage.getDecodedMessage())

                currentSocket?.let {

                    val message = ChatMessage(it.remoteDevice.address, Date(), true, "").apply {
                        seenHere = true
                        messageType = MessageType.IMAGE
                        filePath = path
                    }

                    launch(bgContext) {

                        val size = getImageSize(path)
                        message.fileInfo = "${size.width}x${size.height}"
                        message.fileExists = true

                        db.messagesDao().insert(message)

                        launch(uiContext) {

                            fileListener?.onFileSendingFinished()
                            messageListener?.onMessageSent(message)

                            notificationView.dismissFileTransferNotification()
                            currentConversation?.let {
                                shortcutManager.addConversationShortcut(message.deviceAddress, it.displayName, it.color)
                            }
                        }
                    }
                }
            }

            override fun onFileSendingFailed() {

                launch(uiContext) {
                    fileListener?.onFileSendingFailed()
                    notificationView.dismissFileTransferNotification()
                }
            }

            override fun onFileReceivingStarted(file: TransferringFile) {

                launch(uiContext) {

                    fileListener?.onFileReceivingStarted(file.size)

                    currentConversation?.let {

                        val silently = application.currentChat != null && currentSocket != null &&
                                application.currentChat.equals(currentSocket?.remoteDevice?.address)

                        notificationView.showFileTransferNotification(it.displayName, it.deviceName,
                                it.deviceAddress, file, 0, silently, preferences.getSettings())
                    }
                }
            }

            override fun onFileReceivingProgress(file: TransferringFile, receivedBytes: Long) {

                launch(uiContext) {

                    fileListener?.onFileReceivingProgress(receivedBytes, file.size)

                    if (currentConversation != null) {
                        notificationView.updateFileTransferNotification(receivedBytes, file.size)
                    }
                }
            }

            override fun onFileReceivingFinished(path: String) {

                currentSocket?.remoteDevice?.let {

                    val address = it.address
                    val message = ChatMessage(address, Date(), false, "").apply {
                        messageType = MessageType.IMAGE
                        filePath = path
                    }

                    if (messageListener == null || application.currentChat == null || !application.currentChat.equals(address)) {
                        notificationView.dismissMessageNotification()
                        notificationView.showNewMessageNotification(getString(R.string.chat__image_message, "\uD83D\uDCCE"), currentConversation?.displayName,
                                it.name, address, preferences.getSettings())
                    } else {
                        message.seenHere = true
                    }

                    launch(bgContext) {

                        val size = getImageSize(path)
                        message.fileInfo = "${size.width}x${size.height}"
                        message.fileExists = true

                        db.messagesDao().insert(message)

                        launch(uiContext) {
                            fileListener?.onFileReceivingFinished()
                            messageListener?.onMessageReceived(message)

                            notificationView.dismissFileTransferNotification()
                            currentConversation?.let {
                                shortcutManager.addConversationShortcut(address, it.displayName, it.color)
                            }
                        }
                    }
                }
            }

            override fun onFileReceivingFailed() {
                launch(uiContext) {
                    fileListener?.onFileReceivingFailed()
                    notificationView.dismissFileTransferNotification()
                }
            }

            override fun onFileTransferCanceled(byPartner: Boolean) {
                launch(uiContext) {
                    fileListener?.onFileTransferCanceled(byPartner)
                    notificationView.dismissFileTransferNotification()
                }
            }
        }

        val eventsStrategy = TransferEventStrategy()
        val filesDirectory = File(Environment.getExternalStorageDirectory(), getString(R.string.app_name))

        dataTransferThread =
                object : DataTransferThread(socket, type, transferEventsListener, filesDirectory, fileEventsListener, eventsStrategy) {
                    override fun shouldRun(): Boolean {
                        return isConnectedOrPending()
                    }
                }
        dataTransferThread?.prepare()
        dataTransferThread?.start()

        launch(uiContext) { connectionListener?.onConnected(socket.remoteDevice) }
    }

    private fun getImageSize(path: String): Size {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        return Size(options.outWidth, options.outHeight)
    }

    @Synchronized
    fun stop() {
        cancelConnections()
        acceptThread?.cancel()
        acceptThread = null
        connectionState = ConnectionState.NOT_CONNECTED
    }

    private fun cancelConnections() {
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

        if (isConnected()) {
            val startMessage = Message.createFileStartMessage(file, type)
            dataTransferThread?.write(startMessage.getDecodedMessage())
            dataTransferThread?.writeFile(file)
        }
    }

    fun getTransferringFile(): TransferringFile? {
        return dataTransferThread?.getTransferringFile()
    }

    fun cancelFileTransfer() {
        dataTransferThread?.cancelFileTransfer()
        notificationView.dismissFileTransferNotification()
    }

    private fun onMessageSent(messageBody: String) = currentSocket?.let {

        val message = Message(messageBody)
        val sentMessage = ChatMessage(it.remoteDevice.address, Date(), true, message.body)

        if (message.type == Message.Type.MESSAGE) {
            sentMessage.seenHere = true
            launch(bgContext) {
                db.messagesDao().insert(sentMessage)
                launch(uiContext) { messageListener?.onMessageSent(sentMessage) }
                currentConversation?.let {
                    shortcutManager.addConversationShortcut(sentMessage.deviceAddress, it.displayName, it.color)
                }
            }
        }
    }

    private fun onMessageReceived(messageBody: String) {

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
        } else if (message.type == Message.Type.FILE_CANCELED) {
            dataTransferThread?.cancelFileTransfer()
        }
    }

    private fun handleReceivedMessage(text: String) = currentSocket?.let {

        val device: BluetoothDevice = it.remoteDevice

        val receivedMessage = ChatMessage(device.address, Date(), false, text)

        if (messageListener == null || application.currentChat == null || !application.currentChat.equals(device.address)) {
            notificationView.showNewMessageNotification(text, currentConversation?.displayName,
                    device.name, device.address, preferences.getSettings())
        } else {
            receivedMessage.seenHere = true
        }

        launch(bgContext) {
            db.messagesDao().insert(receivedMessage)
            launch(uiContext) { messageListener?.onMessageReceived(receivedMessage) }
            currentConversation?.let {
                shortcutManager.addConversationShortcut(device.address, it.displayName, it.color)
            }
        }
    }

    private fun handleConnectionRequest(message: Message) = currentSocket?.let {

        val device: BluetoothDevice = it.remoteDevice

        val parts = message.body.split("#")
        val conversation = Conversation(device.address, device.name ?: "?", parts[0], parts[1].toInt())

        val messageContractVersion = if (parts.size >= 3) parts[2].toInt() else 0
        conversation.messageContractVersion = messageContractVersion

        launch(bgContext) { db.conversationsDao().insert(conversation) }

        currentConversation = conversation

        connectionListener?.onConnectedIn(conversation)

        if (!application.isConversationsOpened && !(application.currentChat != null && application.currentChat.equals(device.address))) {
            notificationView.showConnectionRequestNotification(
                    "${conversation.displayName} (${conversation.deviceName})", preferences.getSettings())
        }
    }

    private fun handleConnectionApproval(message: Message) = currentSocket?.let {

        val device: BluetoothDevice = it.remoteDevice

        val parts = message.body.split("#")
        val conversation = Conversation(device.address, device.name ?: "?", parts[0], parts[1].toInt())

        val messageContractVersion = if (parts.size >= 3) parts[2].toInt() else 0
        conversation.messageContractVersion = messageContractVersion

        launch(bgContext) { db.conversationsDao().insert(conversation) }

        currentConversation = conversation

        connectionState = ConnectionState.CONNECTED
        connectionListener?.onConnectionAccepted()
        connectionListener?.onConnectedOut(conversation)
    }

    private fun connectionFailed() {
        currentSocket = null
        currentConversation = null
        launch(uiContext) { connectionListener?.onConnectionFailed() }
        connectionState = ConnectionState.NOT_CONNECTED
        prepareForAccept()
    }

    private fun connectionLost() {

        currentSocket = null
        currentConversation = null
        if (isConnectedOrPending()) {
            launch(uiContext) {
                if (isPending() && connectionType == ConnectionType.INCOMING) {
                    connectionState = ConnectionState.NOT_CONNECTED
                    connectionListener?.onConnectionWithdrawn()
                } else {
                    connectionState = ConnectionState.NOT_CONNECTED
                    connectionListener?.onConnectionLost()
                }
                prepareForAccept()
            }
        } else {
            prepareForAccept()
        }
    }

    fun isConnected() = connectionState == ConnectionState.CONNECTED

    fun isPending() = connectionState == ConnectionState.PENDING

    fun isConnectedOrPending() = isConnected() || isPending()

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
                e.printStackTrace()
            }

            connectionState = ConnectionState.LISTENING
        }

        override fun run() {

            var socket: BluetoothSocket?

            while (!isConnectedOrPending()) {

                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }

                socket?.let {
                    when (connectionState) {
                        ConnectionState.LISTENING, ConnectionState.CONNECTING -> {
                            connected(it, ConnectionType.INCOMING)
                        }
                        ConnectionState.NOT_CONNECTED, ConnectionState.CONNECTED, ConnectionState.PENDING, ConnectionState.REJECTED -> try {
                            it.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class ConnectThread(private val bluetoothDevice: BluetoothDevice) : Thread() {

        private var socket: BluetoothSocket? = null

        override fun run() {

            try {
                socket = bluetoothDevice.createRfcommSocketToServiceRecord(APP_UUID)
            } catch (e: IOException) {
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
                    e.printStackTrace()
                }
                connectionFailed()
                return
            }

            synchronized(this@BluetoothConnectionService) {
                connectThread = null
            }

            socket?.let {
                connected(it, ConnectionType.OUTCOMING)
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
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

        private const val FOREGROUND_SERVICE = 101
        const val ACTION_STOP = "action.stop"

        fun start(context: Context) {
            val intent = Intent(context, BluetoothConnectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun bind(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, BluetoothConnectionService::class.java)
            context.bindService(intent, connection, AppCompatActivity.BIND_ABOVE_CLIENT)
        }
    }
}
