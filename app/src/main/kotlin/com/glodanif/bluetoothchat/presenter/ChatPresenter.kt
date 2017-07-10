package com.glodanif.bluetoothchat.presenter

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

            updateState()
        }

        override fun onError() {
            onStop()
        }
    }

    private val connectionListener = object : OnConnectionListener {

        override fun onConnectionDestroyed() {
            view.showServiceDestroyed()
        }

        override fun onConnectionAccepted() {
            view.showAcceptedConnection()
            view.hideActions()
        }

        override fun onConnectionRejected() {
            view.showRejectedConnection()
            updateState()
        }

        override fun onConnectedIn(conversation: Conversation) {
            view.notifyAboutConnectedDevice(conversation)
        }

        override fun onConnectedOut(conversation: Conversation) {

        }

        override fun onConnecting() {

        }

        override fun onConnectionLost() {
            view.showLostConnection()
            updateState()
        }

        override fun onConnectionFailed() {
            view.showFailedConnection()
            updateState()
        }

        override fun onDisconnected() {
            view.showDisconnected()
            updateState()
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

        if (!connectionModel.isConnectedOrPending()) {
            connectionModel.prepare()
        } else {
            updateState()
        }

        storage.getMessagesByDevice(deviceAddress) {
            it.forEach { it.seenHere = true }
            storage.updateMessages(it)
            view.showMessagesHistory(it)
        }
    }

    fun onStop() {

        connectionModel.setOnPrepareListener(null)
        connectionModel.setOnConnectListener(null)
        connectionModel.setOnMessageListener(null)

        if (!connectionModel.isConnectedOrPending()) {
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
            view.showWainingForOpponent()
        } else {
            view.showDeviceIsNotAvailable()
        }
    }

    fun sendMessage(message: String) {

        if (!connectionModel.isConnected()) {
            view.showNotConnectedToSend()
            return
        }

        if (message.isNullOrEmpty()) {
            view.showNotValidMessage()
        } else {
            connectionModel.sendMessage(message)
            view.afterMessageSent()
        }
    }

    fun reconnect() {
        connectToDevice()
        view.showWainingForOpponent()
    }

    fun acceptConnection() {
        view.hideActions()
        view.showConnected()
        connectionModel.acceptConnection()
    }

    fun rejectConnection() {
        view.hideActions()
        updateState()
        connectionModel.rejectConnection()
    }

    private fun updateState() {
        val currentConversation: Conversation? = connectionModel.getCurrentConversation()
        if (currentConversation == null) {
            if (connectionModel.isPending()) {
                view.showWainingForOpponent()
            } else {
                view.showNotConnectedToAnyDevice()
            }
        } else if (currentConversation.deviceAddress != deviceAddress) {
            view.showNotConnectedToThisDevice(currentConversation.deviceAddress)
        } else if (connectionModel.isPending() && currentConversation.deviceAddress == deviceAddress) {
            view.notifyAboutConnectedDevice(currentConversation)
        }
    }
}
