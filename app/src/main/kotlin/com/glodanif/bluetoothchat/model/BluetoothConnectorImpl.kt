package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.entity.Message
import com.glodanif.bluetoothchat.service.BluetoothConnectionService
import java.lang.IllegalStateException

class BluetoothConnectorImpl(private val context: Context) : BluetoothConnector {

    private var prepareListener: OnPrepareListener? = null
    private var connectListener: OnConnectionListener? = null
    private var messageListener: OnMessageListener? = null

    private var service: BluetoothConnectionService? = null
    private var bound = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as BluetoothConnectionService.ConnectionBinder).getService()
            service?.setConnectionListener(connectionListenerInner)
            service?.setMessageListener(messageListenerInner)

            bound = true
            prepareListener?.onPrepared()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service?.setConnectionListener(null)
            service?.setMessageListener(null)
            service = null

            bound = false
            prepareListener?.onError()
        }
    }

    private val connectionListenerInner = object : OnConnectionListener {

        override fun onConnectionAccepted() {
            connectListener?.onConnectionAccepted()
        }

        override fun onConnectionRejected() {
            connectListener?.onConnectionRejected()
        }

        override fun onConnecting() {
            connectListener?.onConnecting()
        }

        override fun onConnectedIn(device: BluetoothDevice) {
            connectListener?.onConnectedIn(device)
        }

        override fun onConnectedOut(device: BluetoothDevice) {
            connectListener?.onConnectedOut(device)
        }

        override fun onConnectionLost() {
            connectListener?.onConnectionLost()
        }

        override fun onConnectionFailed() {
            connectListener?.onConnectionFailed()
        }

        override fun onDisconnected() {
            connectListener?.onDisconnected()
        }
    }

    private val messageListenerInner = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            messageListener?.onMessageReceived(message)
        }

        override fun onMessageSent(message: ChatMessage) {
            messageListener?.onMessageSent(message)
        }

        override fun onMessageDelivered(id: String) {
            messageListener?.onMessageDelivered(id)
        }

        override fun onMessageNotDelivered(id: String) {
            messageListener?.onMessageNotDelivered(id)
        }

        override fun onMessageSeen(id: String) {
            messageListener?.onMessageSeen(id)
        }
    }

    override fun prepare() {
        if (!BluetoothConnectionService.isRunning) {
            BluetoothConnectionService.start(context)
        }
        BluetoothConnectionService.bind(context, connection)
    }

    override fun release() {
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
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

    override fun connect(device: BluetoothDevice) {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.connect(device)
    }

    override fun stop() {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.stop()
    }

    override fun sendMessage(message: String) {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        val chatMessage = Message(System.nanoTime().toString(), message, Message.Type.MESSAGE)
        service?.sendMessage(chatMessage)
    }

    override fun restart() {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.prepareForAccept()
    }

    override fun isConnected(): Boolean {
        return if (service == null) false else service!!.isConnected()
    }

    override fun getCurrentlyConnectedDevice(): BluetoothDevice? {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        return service?.getCurrentDevice()
    }

    override fun acceptConnection() {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        val message = Message(true, Message.Type.CONNECTION)
        service?.sendMessage(message)
    }

    override fun rejectConnection() {

        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        val message = Message(false, Message.Type.CONNECTION)
        service?.sendMessage(message)
    }
}
