package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ChatView

class ChatPresenter(private val deviceAddress: String, private val view: ChatView,
                    private val connectionModel: BluetoothConnector, private val storage: MessagesStorage) {

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {
            if (connectionModel.getCurrentlyConnectedDevice() != null) {
                view.showConnected()
            }
            connectionModel.setOnConnectListener(connectionListener)
            connectionModel.setOnMessageListener(messageListener)
        }

        override fun onError() {
            onStop()
        }
    }

    private val connectionListener = object : OnConnectionListener {

        override fun onConnectionAccepted() {
            view.showAcceptedConnection()
        }

        override fun onConnectionRejected() {
            view.showRejectedConnection()
        }

        override fun onConnectedIn(device: BluetoothDevice) {

        }

        override fun onConnectedOut(device: BluetoothDevice) {

        }

        override fun onConnecting() {

        }

        override fun onConnectionLost() {
            view.showLostConnection()
        }

        override fun onConnectionFailed() {

        }

        override fun onDisconnected() {
            view.showDisconnected()
        }
    }

    private val messageListener = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            view.showReceivedMessage(message)
        }

        override fun onMessageSent(message: ChatMessage) {
            view.showSentMessage(message)
        }

        override fun onMessageDelivered(id: String) {

        }

        override fun onMessageNotDelivered(id: String) {

        }

        override fun onMessageSeen(id: String) {

        }
    }

    fun onStart() {
        connectionModel.setOnPrepareListener(prepareListener)
        if (!connectionModel.isConnected()) {
            connectionModel.prepare()
        }
        storage.getMessagesByDevice(deviceAddress) { view.showMessagesHistory(it) }
    }

    fun onStop() {

        connectionModel.setOnPrepareListener(null)
        connectionModel.setOnConnectListener(null)
        connectionModel.setOnMessageListener(null)

        if (!connectionModel.isConnected()) {
            connectionModel.release()
        }
    }

    fun sendMessage(message: String) {
        if (message.isNullOrEmpty()) {
            view.showNotValidMessage()
        } else {
            connectionModel.sendMessage(message)
        }
    }

    fun onConnect(device: BluetoothDevice) {
        connectionModel.connect(device)
    }
}
