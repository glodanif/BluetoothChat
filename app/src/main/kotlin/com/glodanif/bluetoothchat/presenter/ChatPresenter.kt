package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ChatView
import android.bluetooth.BluetoothAdapter
import com.glodanif.bluetoothchat.entity.Conversation

class ChatPresenter(private val deviceAddress: String, private val view: ChatView,
                    private val connectionModel: BluetoothConnector, private val storage: MessagesStorage) {

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {
            if (connectionModel.getCurrentConversation() != null) {
                view.showConnected()
            }
            connectionModel.setOnConnectListener(connectionListener)
            connectionModel.setOnMessageListener(messageListener)

            val currentConversation: Conversation? = connectionModel.getCurrentConversation()
            if (currentConversation == null) {
                view.showNotConnectedToAnyDevice()
            } else if (currentConversation.deviceAddress != deviceAddress) {
                view.showNotConnectedToThisDevice(currentConversation.deviceAddress)
            } else if (connectionModel.isPending() && currentConversation.deviceAddress == deviceAddress) {
                view.showWainingForOpponent()
            }
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

        override fun onConnectedIn(conversation: Conversation) {

        }

        override fun onConnectedOut(conversation: Conversation) {

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

    fun disconnect() {
        connectionModel.restart()
        view.showNotConnectedToAnyDevice()
    }

    fun connectToDevice() {

        val adapter = BluetoothAdapter.getDefaultAdapter()
        val device = adapter.bondedDevices
                .filter { it.address.equals(deviceAddress, ignoreCase = true) }
                .first()

        if (device != null) {
            connectionModel.connect(device)
            view.hideActions()
        } else {
            view.showDeviceIsNotAvailable()
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
