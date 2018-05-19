package com.glodanif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.glodanif.bluetoothchat.BuildConfig
import com.glodanif.bluetoothchat.data.entity.*
import com.glodanif.bluetoothchat.data.internal.AutoresponderProxy
import com.glodanif.bluetoothchat.data.internal.CommunicationProxy
import com.glodanif.bluetoothchat.data.internal.EmptyProxy
import com.glodanif.bluetoothchat.data.service.*
import java.io.File

class BluetoothConnectorImpl(private val context: Context) : BluetoothConnector {

    private var prepareListeners = mutableSetOf<OnPrepareListener>()
    private var connectListeners = mutableSetOf<OnConnectionListener>()
    private var messageListeners = mutableSetOf<OnMessageListener>()
    private var fileListeners = mutableSetOf<OnFileListener>()

    private var proxy: CommunicationProxy? = null

    private var service: BluetoothConnectionService? = null
    private var bound = false
    private var isPreparing = false

    private val settings = SettingsManagerImpl(context)

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {

            service = (binder as BluetoothConnectionService.ConnectionBinder).getService().apply {
                setConnectionListener(connectionListenerInner)
                setMessageListener(messageListenerInner)
                setFileListener(fileListenerInner)
            }

            proxy = if (BuildConfig.AUTORESPONDER) AutoresponderProxy(service) else EmptyProxy()

            bound = true
            isPreparing = false
            prepareListeners.forEach { it.onPrepared() }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service?.setConnectionListener(null)
            service?.setMessageListener(null)
            service?.setFileListener(null)
            service = null
            proxy = null

            isPreparing = false
            bound = false
            prepareListeners.forEach { it.onError() }
        }
    }

    private val connectionListenerInner = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {
            proxy?.onConnected(device)
            connectListeners.forEach { it.onConnected(device) }
        }

        override fun onConnectionWithdrawn() {
            proxy?.onConnectionWithdrawn()
            connectListeners.forEach { it.onConnectionWithdrawn() }
        }

        override fun onConnectionAccepted() {
            proxy?.onConnectionAccepted()
            connectListeners.forEach { it.onConnectionAccepted() }
        }

        override fun onConnectionRejected() {
            proxy?.onConnectionRejected()
            connectListeners.forEach { it.onConnectionRejected() }
        }

        override fun onConnecting() {
            proxy?.onConnecting()
            connectListeners.forEach { it.onConnecting() }
        }

        override fun onConnectedIn(conversation: Conversation) {
            proxy?.onConnectedIn(conversation)
            connectListeners.forEach { it.onConnectedIn(conversation) }
        }

        override fun onConnectedOut(conversation: Conversation) {
            proxy?.onConnectedOut(conversation)
            connectListeners.forEach { it.onConnectedOut(conversation) }
        }

        override fun onConnectionLost() {
            proxy?.onConnectionLost()
            connectListeners.forEach { it.onConnectionLost() }
        }

        override fun onConnectionFailed() {
            proxy?.onConnectionFailed()
            connectListeners.forEach { it.onConnectionFailed() }
        }

        override fun onDisconnected() {
            proxy?.onDisconnected()
            connectListeners.forEach { it.onDisconnected() }
        }

        override fun onConnectionDestroyed() {
            proxy?.onConnectionDestroyed()
            connectListeners.forEach { it.onConnectionDestroyed() }
            release()
        }
    }

    private val messageListenerInner = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            proxy?.onMessageReceived(message)
            messageListeners.forEach { it.onMessageReceived(message) }
        }

        override fun onMessageSent(message: ChatMessage) {
            proxy?.onMessageSent(message)
            messageListeners.forEach { it.onMessageSent(message) }
        }

        override fun onMessageDelivered(id: Long) {
            proxy?.onMessageDelivered(id)
            messageListeners.forEach { it.onMessageDelivered(id) }
        }

        override fun onMessageNotDelivered(id: Long) {
            proxy?.onMessageNotDelivered(id)
            messageListeners.forEach { it.onMessageNotDelivered(id) }
        }

        override fun onMessageSeen(id: Long) {
            proxy?.onMessageSeen(id)
            messageListeners.forEach { it.onMessageSeen(id) }
        }
    }

    private val fileListenerInner = object : OnFileListener {

        override fun onFileSendingStarted(fileAddress: String?, fileSize: Long) {
            proxy?.onFileSendingStarted(fileAddress, fileSize)
            fileListeners.forEach { it.onFileSendingStarted(fileAddress, fileSize) }
        }

        override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {
            proxy?.onFileSendingProgress(sentBytes, totalBytes)
            fileListeners.forEach { it.onFileSendingProgress(sentBytes, totalBytes) }
        }

        override fun onFileSendingFinished() {
            proxy?.onFileSendingFinished()
            fileListeners.forEach { it.onFileSendingFinished() }
        }

        override fun onFileSendingFailed() {
            proxy?.onFileSendingFailed()
            fileListeners.forEach { it.onFileSendingFailed() }
        }

        override fun onFileReceivingStarted(fileSize: Long) {
            proxy?.onFileReceivingStarted(fileSize)
            fileListeners.forEach { it.onFileReceivingStarted(fileSize) }
        }

        override fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long) {
            proxy?.onFileReceivingProgress(sentBytes, totalBytes)
            fileListeners.forEach { it.onFileReceivingProgress(sentBytes, totalBytes) }
        }

        override fun onFileReceivingFinished() {
            proxy?.onFileReceivingFinished()
            fileListeners.forEach { it.onFileReceivingFinished() }
        }

        override fun onFileReceivingFailed() {
            proxy?.onFileReceivingFailed()
            fileListeners.forEach { it.onFileReceivingFailed() }
        }

        override fun onFileTransferCanceled(byPartner: Boolean) {
            proxy?.onFileTransferCanceled(byPartner)
            fileListeners.forEach { it.onFileTransferCanceled(byPartner) }
        }
    }

    override fun prepare() {

        if (isPreparing) return
        isPreparing = true

        bound = false
        if (!BluetoothConnectionService.isRunning) {
            BluetoothConnectionService.start(context)
        }
        BluetoothConnectionService.bind(context, connection)
    }

    override fun release() {

        if (bound) {
            context.unbindService(connection)
        }

        bound = false
        service = null
        proxy = null

        connectListeners.clear()
        prepareListeners.clear()
        messageListeners.clear()
        fileListeners.clear()
    }

    override fun isConnectionPrepared(): Boolean {
        return bound
    }

    override fun addOnPrepareListener(listener: OnPrepareListener) {
        this.prepareListeners.add(listener)
    }

    override fun addOnConnectListener(listener: OnConnectionListener) {
        this.connectListeners.add(listener)
    }

    override fun addOnMessageListener(listener: OnMessageListener) {
        this.messageListeners.add(listener)
    }

    override fun addOnFileListener(listener: OnFileListener) {
        this.fileListeners.add(listener)
    }

    override fun removeOnPrepareListener(listener: OnPrepareListener) {
        this.prepareListeners.remove(listener)
    }

    override fun removeOnConnectListener(listener: OnConnectionListener) {
        this.connectListeners.remove(listener)
    }

    override fun removeOnMessageListener(listener: OnMessageListener) {
        this.messageListeners.remove(listener)
    }

    override fun removeOnFileListener(listener: OnFileListener) {
        this.fileListeners.remove(listener)
    }

    override fun connect(device: BluetoothDevice) {
        service?.connect(device)
    }

    override fun stop() {
        service?.stop()
    }

    override fun sendMessage(messageText: String) {

        service?.getCurrentContract()?.createChatMessage(messageText)?.let { message ->
            service?.sendMessage(message)
        }
    }

    override fun sendFile(file: File, type: PayloadType) {
        service?.sendFile(file, type)
    }

    override fun getTransferringFile(): TransferringFile? {
        return service?.getTransferringFile()
    }

    override fun cancelFileTransfer() {
        service?.cancelFileTransfer()
    }

    override fun restart() {
        service?.disconnect()
    }

    override fun isConnected(): Boolean {
        return service?.isConnected() ?: false
    }

    override fun isConnectedOrPending(): Boolean {
        return service?.isConnectedOrPending() ?: false
    }

    override fun isPending(): Boolean {
        return service?.isPending() ?: false
    }

    override fun getCurrentConversation(): Conversation? {
        return service?.getCurrentConversation()
    }

    override fun acceptConnection() {

        service?.getCurrentContract()?.createAcceptConnectionMessage(settings.getUserName(), settings.getUserColor())?.let { message ->
            service?.sendMessage(message)
        }
    }

    override fun rejectConnection() {

        service?.getCurrentContract()?.createRejectConnectionMessage(settings.getUserName(), settings.getUserColor())?.let { message ->
            service?.sendMessage(message)
        }
    }

    override fun sendDisconnectRequest() {

        service?.getCurrentContract()?.createDisconnectMessage()?.let { message ->
            service?.sendMessage(message)
        }
    }

    override fun isFeatureAvailable(feature: Contract.Feature) =
            service?.getCurrentContract()?.isFeatureAvailable(feature) ?: true

}
