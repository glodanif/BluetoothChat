package com.glodanif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.glodanif.bluetoothchat.BuildConfig
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.internal.AutoresponderProxy
import com.glodanif.bluetoothchat.data.internal.CommunicationProxy
import com.glodanif.bluetoothchat.data.internal.EmptyProxy
import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.utils.safeRemove
import java.io.File

class BluetoothConnectorImpl(private val context: Context) : BluetoothConnector {

    private val monitor = Any()

    private var prepareListeners = LinkedHashSet<OnPrepareListener>()
    private var connectListeners = LinkedHashSet<OnConnectionListener>()
    private var messageListeners = LinkedHashSet<OnMessageListener>()
    private var fileListeners = LinkedHashSet<OnFileListener>()

    private var proxy: CommunicationProxy? = null

    private var service: BluetoothConnectionService? = null
    private var bound = false
    private var isPreparing = false

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
            synchronized(monitor) {
                prepareListeners.forEach { it.onPrepared() }
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service?.setConnectionListener(null)
            service?.setMessageListener(null)
            service?.setFileListener(null)
            service = null
            proxy = null

            isPreparing = false
            bound = false
            synchronized(monitor) {
                prepareListeners.forEach { it.onError() }
            }
        }
    }

    private val connectionListenerInner = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {
            proxy?.onConnected(device)
            synchronized(monitor) {
                connectListeners.forEach { it.onConnected(device) }
            }
        }

        override fun onConnectionWithdrawn() {
            proxy?.onConnectionWithdrawn()
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectionWithdrawn() }
            }
        }

        override fun onConnectionAccepted() {
            proxy?.onConnectionAccepted()
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectionAccepted() }
            }
        }

        override fun onConnectionRejected() {
            proxy?.onConnectionRejected()
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectionRejected() }
            }
        }

        override fun onConnecting() {
            proxy?.onConnecting()
            synchronized(monitor) {
                connectListeners.forEach { it.onConnecting() }
            }
        }

        override fun onConnectedIn(conversation: Conversation) {
            proxy?.onConnectedIn(conversation)
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectedIn(conversation) }
            }
        }

        override fun onConnectedOut(conversation: Conversation) {
            proxy?.onConnectedOut(conversation)
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectedOut(conversation) }
            }
        }

        override fun onConnectionLost() {
            proxy?.onConnectionLost()
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectionLost() }
            }
        }

        override fun onConnectionFailed() {
            proxy?.onConnectionFailed()
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectionFailed() }
            }
        }

        override fun onDisconnected() {
            proxy?.onDisconnected()
            synchronized(monitor) {
                connectListeners.forEach { it.onDisconnected() }
            }
        }

        override fun onConnectionDestroyed() {
            proxy?.onConnectionDestroyed()
            synchronized(monitor) {
                connectListeners.forEach { it.onConnectionDestroyed() }
            }
            release()
        }
    }

    private val messageListenerInner = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            proxy?.onMessageReceived(message)
            synchronized(monitor) {
                messageListeners.forEach { it.onMessageReceived(message) }
            }
        }

        override fun onMessageSent(message: ChatMessage) {
            proxy?.onMessageSent(message)
            synchronized(monitor) {
                messageListeners.forEach { it.onMessageSent(message) }
            }
        }

        override fun onMessageSendingFailed() {
            proxy?.onMessageSendingFailed()
            synchronized(monitor) {
                messageListeners.forEach { it.onMessageSendingFailed() }
            }
        }

        override fun onMessageDelivered(id: Long) {
            proxy?.onMessageDelivered(id)
            synchronized(monitor) {
                messageListeners.forEach { it.onMessageDelivered(id) }
            }
        }

        override fun onMessageNotDelivered(id: Long) {
            proxy?.onMessageNotDelivered(id)
            synchronized(monitor) {
                messageListeners.forEach { it.onMessageNotDelivered(id) }
            }
        }

        override fun onMessageSeen(id: Long) {
            proxy?.onMessageSeen(id)
            synchronized(monitor) {
                messageListeners.forEach { it.onMessageSeen(id) }
            }
        }
    }

    private val fileListenerInner = object : OnFileListener {

        override fun onFileSendingStarted(fileAddress: String?, fileSize: Long) {
            proxy?.onFileSendingStarted(fileAddress, fileSize)
            synchronized(monitor) {
                fileListeners.forEach { it.onFileSendingStarted(fileAddress, fileSize) }
            }
        }

        override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {
            proxy?.onFileSendingProgress(sentBytes, totalBytes)
            synchronized(monitor) {
                fileListeners.forEach { it.onFileSendingProgress(sentBytes, totalBytes) }
            }
        }

        override fun onFileSendingFinished() {
            proxy?.onFileSendingFinished()
            synchronized(monitor) {
                fileListeners.forEach { it.onFileSendingFinished() }
            }
        }

        override fun onFileSendingFailed() {
            proxy?.onFileSendingFailed()
            synchronized(monitor) {
                fileListeners.forEach { it.onFileSendingFailed() }
            }
        }

        override fun onFileReceivingStarted(fileSize: Long) {
            proxy?.onFileReceivingStarted(fileSize)
            synchronized(monitor) {
                fileListeners.forEach { it.onFileReceivingStarted(fileSize) }
            }
        }

        override fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long) {
            proxy?.onFileReceivingProgress(sentBytes, totalBytes)
            synchronized(monitor) {
                fileListeners.forEach { it.onFileReceivingProgress(sentBytes, totalBytes) }
            }
        }

        override fun onFileReceivingFinished() {
            proxy?.onFileReceivingFinished()
            synchronized(monitor) {
                fileListeners.forEach { it.onFileReceivingFinished() }
            }
        }

        override fun onFileReceivingFailed() {
            proxy?.onFileReceivingFailed()
            synchronized(monitor) {
                fileListeners.forEach { it.onFileReceivingFailed() }
            }
        }

        override fun onFileTransferCanceled(byPartner: Boolean) {
            proxy?.onFileTransferCanceled(byPartner)
            synchronized(monitor) {
                fileListeners.forEach { it.onFileTransferCanceled(byPartner) }
            }
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

        synchronized(monitor) {
            connectListeners = LinkedHashSet()
            prepareListeners = LinkedHashSet()
            messageListeners = LinkedHashSet()
            fileListeners = LinkedHashSet()
        }
    }

    override fun isConnectionPrepared() = bound

    override fun addOnPrepareListener(listener: OnPrepareListener) {
        synchronized(monitor) {
            prepareListeners.add(listener)
        }
    }

    override fun addOnConnectListener(listener: OnConnectionListener) {
        synchronized(monitor) {
            connectListeners.add(listener)
        }
    }

    override fun addOnMessageListener(listener: OnMessageListener) {
        synchronized(monitor) {
            messageListeners.add(listener)
        }
    }

    override fun addOnFileListener(listener: OnFileListener) {
        synchronized(monitor) {
            fileListeners.add(listener)
        }
    }

    override fun removeOnPrepareListener(listener: OnPrepareListener) {
        synchronized(monitor) {
            prepareListeners.safeRemove(listener)
        }
    }

    override fun removeOnConnectListener(listener: OnConnectionListener) {
        synchronized(monitor) {
            connectListeners.safeRemove(listener)
        }
    }

    override fun removeOnMessageListener(listener: OnMessageListener) {
        synchronized(monitor) {
            messageListeners.safeRemove(listener)
        }
    }

    override fun removeOnFileListener(listener: OnFileListener) {
        synchronized(monitor) {
            fileListeners.safeRemove(listener)
        }
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

    override fun getTransferringFile() = service?.getTransferringFile()

    override fun cancelFileTransfer() {
        service?.cancelFileTransfer()
    }

    override fun disconnect() {
        service?.disconnect()
    }

    override fun isConnected() = service?.isConnected() ?: false

    override fun isConnectedOrPending() = service?.isConnectedOrPending() ?: false

    override fun isPending() = service?.isPending() ?: false

    override fun getCurrentConversation() = service?.getCurrentConversation()

    override fun acceptConnection() {
        service?.approveConnection()
    }

    override fun rejectConnection() {
        service?.rejectConnection()
    }

    override fun sendDisconnectRequest() {

        service?.getCurrentContract()?.createDisconnectMessage()?.let { message ->
            service?.sendMessage(message)
        }
    }

    override fun isFeatureAvailable(feature: Contract.Feature) =
            service?.getCurrentContract()?.isFeatureAvailable(feature) ?: true

}
