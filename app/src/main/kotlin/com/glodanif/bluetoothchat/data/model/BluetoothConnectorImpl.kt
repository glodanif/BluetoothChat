package com.glodanif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.support.test.espresso.IdlingResource
import com.glodanif.bluetoothchat.BuildConfig
import com.glodanif.bluetoothchat.data.entity.*
import com.glodanif.bluetoothchat.data.internal.AutoresponderProxy
import com.glodanif.bluetoothchat.data.internal.CommunicationProxy
import com.glodanif.bluetoothchat.data.internal.EmptyProxy
import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import java.io.File
import com.glodanif.bluetoothchat.data.internal.SimpleIdlingResource
import android.support.annotation.NonNull
import android.support.annotation.VisibleForTesting

class BluetoothConnectorImpl(private val context: Context) : BluetoothConnector {

    private var prepareListener: OnPrepareListener? = null
    private var messageListener: OnMessageListener? = null
    private var connectListener: OnConnectionListener? = null
    private var fileListener: OnFileListener? = null

    private var proxy: CommunicationProxy? = null

    private var service: BluetoothConnectionService? = null
    private var bound = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {

            service = (binder as BluetoothConnectionService.ConnectionBinder).getService().apply {
                setConnectionListener(connectionListenerInner)
                setMessageListener(messageListenerInner)
                setFileListener(fileListenerInner)
            }

            proxy = if (BuildConfig.AUTORESPONDER) AutoresponderProxy(service) else EmptyProxy()

            bound = true
            prepareListener?.onPrepared()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service?.setConnectionListener(null)
            service?.setMessageListener(null)
            service?.setFileListener(null)
            service = null
            proxy = null

            bound = false
            prepareListener?.onError()
        }
    }

    private val connectionListenerInner = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {
            proxy?.onConnected(device)
            connectListener?.onConnected(device)
        }

        override fun onConnectionWithdrawn() {
            proxy?.onConnectionWithdrawn()
            connectListener?.onConnectionWithdrawn()
        }

        override fun onConnectionAccepted() {
            proxy?.onConnectionAccepted()
            connectListener?.onConnectionAccepted()
        }

        override fun onConnectionRejected() {
            proxy?.onConnectionRejected()
            connectListener?.onConnectionRejected()
        }

        override fun onConnecting() {
            proxy?.onConnecting()
            connectListener?.onConnecting()
        }

        override fun onConnectedIn(conversation: Conversation) {
            proxy?.onConnectedIn(conversation)
            connectListener?.onConnectedIn(conversation)
        }

        override fun onConnectedOut(conversation: Conversation) {
            proxy?.onConnectedOut(conversation)
            connectListener?.onConnectedOut(conversation)
        }

        override fun onConnectionLost() {
            proxy?.onConnectionLost()
            connectListener?.onConnectionLost()
        }

        override fun onConnectionFailed() {
            proxy?.onConnectionFailed()
            connectListener?.onConnectionFailed()
        }

        override fun onDisconnected() {
            proxy?.onDisconnected()
            connectListener?.onDisconnected()
        }

        override fun onConnectionDestroyed() {
            proxy?.onConnectionDestroyed()
            connectListener?.onConnectionDestroyed()
        }
    }

    private val messageListenerInner = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            proxy?.onMessageReceived(message)
            messageListener?.onMessageReceived(message)
        }

        override fun onMessageSent(message: ChatMessage) {
            proxy?.onMessageSent(message)
            messageListener?.onMessageSent(message)
        }

        override fun onMessageDelivered(id: String) {
            proxy?.onMessageDelivered(id)
            messageListener?.onMessageDelivered(id)
        }

        override fun onMessageNotDelivered(id: String) {
            proxy?.onMessageNotDelivered(id)
            messageListener?.onMessageNotDelivered(id)
        }

        override fun onMessageSeen(id: String) {
            proxy?.onMessageSeen(id)
            messageListener?.onMessageSeen(id)
        }
    }

    private val fileListenerInner = object : OnFileListener {

        override fun onFileSendingStarted(fileAddress: String?, fileSize: Long) {
            proxy?.onFileSendingStarted(fileAddress, fileSize)
            fileListener?.onFileSendingStarted(fileAddress, fileSize)
        }

        override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {
            proxy?.onFileSendingProgress(sentBytes, totalBytes)
            fileListener?.onFileSendingProgress(sentBytes, totalBytes)
        }

        override fun onFileSendingFinished() {
            proxy?.onFileSendingFinished()
            fileListener?.onFileSendingFinished()
        }

        override fun onFileSendingFailed() {
            proxy?.onFileSendingFailed()
            fileListener?.onFileSendingFailed()
        }

        override fun onFileReceivingStarted(fileSize: Long) {
            proxy?.onFileReceivingStarted(fileSize)
            fileListener?.onFileReceivingStarted(fileSize)
        }

        override fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long) {
            proxy?.onFileReceivingProgress(sentBytes, totalBytes)
            fileListener?.onFileReceivingProgress(sentBytes, totalBytes)
        }

        override fun onFileReceivingFinished() {
            proxy?.onFileReceivingFinished()
            fileListener?.onFileReceivingFinished()
        }

        override fun onFileReceivingFailed() {
            proxy?.onFileReceivingFailed()
            fileListener?.onFileReceivingFailed()
        }

        override fun onFileTransferCanceled(byPartner: Boolean) {
            proxy?.onFileTransferCanceled(byPartner)
            fileListener?.onFileTransferCanceled(byPartner)
        }
    }

    override fun prepare() {
        service = null
        proxy = null
        bound = false
        if (!BluetoothConnectionService.isRunning) {
            BluetoothConnectionService.start(context)
        }
        BluetoothConnectionService.bind(context, connection)
    }

    override fun release() {
        if (bound) {
            context.unbindService(connection)
            bound = false
            service = null
            proxy = null
        }
    }

    override fun isConnectionPrepared(): Boolean {
        return bound
    }

    override fun setOnConnectListener(listener: OnConnectionListener?) {
        this.connectListener = listener
    }

    override fun setOnPrepareListener(listener: OnPrepareListener?) {
        this.prepareListener = listener
    }

    override fun setOnMessageListener(listener: OnMessageListener?) {
        this.messageListener = listener
    }

    override fun setOnFileListener(listener: OnFileListener?) {
        this.fileListener = listener
    }

    override fun connect(device: BluetoothDevice) {
        service?.connect(device)
    }

    override fun stop() {
        service?.stop()
    }

    override fun sendMessage(message: String) {
        val chatMessage = Message(System.nanoTime().toString(), message, Message.Type.MESSAGE)
        service?.sendMessage(chatMessage)
    }

    override fun sendFile(file: File) {
        service?.sendFile(file, MessageType.IMAGE)
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
        val settings = SettingsManagerImpl(context)
        val message = Message.createAcceptConnectionMessage(settings.getUserName(), settings.getUserColor())
        service?.sendMessage(message)
    }

    override fun rejectConnection() {
        val settings = SettingsManagerImpl(context)
        val message = Message.createRejectConnectionMessage(settings.getUserName(), settings.getUserColor())
        service?.sendMessage(message)
    }

    override fun sendDisconnectRequest() {
        val message = Message.createDisconnectMessage()
        service?.sendMessage(message)
    }
}
