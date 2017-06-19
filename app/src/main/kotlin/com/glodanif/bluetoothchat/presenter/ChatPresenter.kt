package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ChatView

class ChatPresenter(private val deviceAddress: String, private val view: ChatView,
                    private val connectionModel: BluetoothConnector, private val storage: MessagesStorage) {

    init {
        connectionModel.setOnPrepareListener(object : OnPrepareListener {

            override fun onPrepared() {
                if (connectionModel.getCurrentlyConnectedDevice() != null) {
                    view.showConnected()
                }
                connectionModel.setConnectedToUI(true)
            }

            override fun onError() {
                connectionModel.setConnectedToUI(false)
            }
        })

        connectionModel.setOnConnectListener(object : OnConnectionListener {

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
        })

        connectionModel.setOnMessageListener(object : OnMessageListener {

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
        })
    }

    fun onStart() {
        if (!connectionModel.isConnected()) {
            connectionModel.prepare()
        }
        storage.getMessagesByDevice(deviceAddress) { view.showMessagesHistory(it) }
    }

    fun onStop() {
        if (!connectionModel.isConnected()) {
            connectionModel.release()
        }
        connectionModel.setConnectedToUI(false)
    }

    fun sendMessage(message: String) {
        connectionModel.sendMessage(message)
    }

    fun onConnect(device: BluetoothDevice) {
        connectionModel.connect(device)
    }
}
