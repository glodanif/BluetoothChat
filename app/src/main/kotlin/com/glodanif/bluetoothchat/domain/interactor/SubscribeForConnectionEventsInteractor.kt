package com.glodanif.bluetoothchat.domain.interactor

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.OnConnectionListener

class SubscribeForConnectionEventsInteractor(private val connection: BluetoothConnector) : BaseInteractor<Unit, Unit>() {

    override suspend fun execute(input: Unit) {}

    private var listener: OnConnectionListener? = null

    fun subscribe(
            onConnected: ((BluetoothDevice) -> Unit)? = null,
            onConnectionWithdrawn: (() -> Unit)? = null,
            onConnectionDestroyed: (() -> Unit)? = null,
            onConnectionAccepted: (() -> Unit)? = null,
            onConnectionRejected: (() -> Unit)? = null,
            onConnectedIn: ((Conversation) -> Unit)? = null,
            onConnectedOut: ((Conversation) -> Unit)? = null,
            onConnecting: (() -> Unit)? = null,
            onConnectionLost: (() -> Unit)? = null,
            onConnectionFailed: (() -> Unit)? = null,
            onDisconnected: (() -> Unit)? = null
    ) {

        val connectionListener = object : OnConnectionListener {

            override fun onConnected(device: BluetoothDevice) {
                onConnected?.invoke(device)
            }

            override fun onConnectionWithdrawn() {
                onConnectionWithdrawn?.invoke()
            }

            override fun onConnectionDestroyed() {
                onConnectionDestroyed?.invoke()
            }

            override fun onConnectionAccepted() {
                onConnectionAccepted?.invoke()
            }

            override fun onConnectionRejected() {
                onConnectionRejected?.invoke()
            }

            override fun onConnectedIn(conversation: Conversation) {
                onConnectedIn?.invoke(conversation)
            }

            override fun onConnectedOut(conversation: Conversation) {
                onConnectedOut?.invoke(conversation)
            }

            override fun onConnecting() {
                onConnecting?.invoke()
            }

            override fun onConnectionLost() {
                onConnectionLost?.invoke()
            }

            override fun onConnectionFailed() {
                onConnectionFailed?.invoke()
            }

            override fun onDisconnected() {
                onDisconnected?.invoke()
            }
        }

        listener = connectionListener
        connection.addOnConnectListener(connectionListener)
    }

    fun unsubscribe() {
        listener?.let {
            connection.removeOnConnectListener(it)
            listener = null
        }
    }
}
