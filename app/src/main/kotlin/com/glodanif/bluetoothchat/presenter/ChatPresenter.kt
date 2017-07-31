package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ChatView
import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.Conversation

class ChatPresenter(private val deviceAddress: String, private val view: ChatView, private val scanModel: BluetoothScanner,
                    private val connectionModel: BluetoothConnector, private val conversations: ConversationsStorage, private val storage: MessagesStorage) {

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {
            connectionModel.setOnConnectListener(connectionListener)
            connectionModel.setOnMessageListener(messageListener)
            updateState()
        }

        override fun onError() {
            releaseConnection()
        }
    }

    private val connectionListener = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {

        }

        override fun onConnectionWithdrawn() {
            updateState()
        }

        override fun onConnectionDestroyed() {
            view.showServiceDestroyed()
        }

        override fun onConnectionAccepted() {
            view.showStatusConnected()
            view.hideActions()

            conversations.getConversationByAddress(deviceAddress) {
                if (it != null) {
                    view.showPartnerName(it.displayName, it.deviceName)
                }
            }
        }

        override fun onConnectionRejected() {
            view.showRejectedConnection()
            updateState()
        }

        override fun onConnectedIn(conversation: Conversation) {
            val currentConversation: Conversation? = connectionModel.getCurrentConversation()
            if (currentConversation?.deviceAddress == deviceAddress) {
                view.showStatusPending()
                view.showConnectionRequest(conversation)
            }
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

    fun prepareConnection() {

        if (!scanModel.isBluetoothEnabled()) {
            view.showBluetoothDisabled()
        } else {

            connectionModel.setOnPrepareListener(prepareListener)

            if (connectionModel.isConnectionPrepared()) {
                connectionModel.setOnConnectListener(connectionListener)
                connectionModel.setOnMessageListener(messageListener)
                updateState()
            } else {
                connectionModel.prepare()
            }
        }

        storage.getMessagesByDevice(deviceAddress) {
            it.forEach { it.seenHere = true }
            storage.updateMessages(it)
            view.showMessagesHistory(it)
        }

        conversations.getConversationByAddress(deviceAddress) {
            if (it != null) {
                view.showPartnerName(it.displayName, it.deviceName)
            }
        }
    }

    fun releaseConnection() {

        connectionModel.setOnPrepareListener(null)
        connectionModel.setOnConnectListener(null)
        connectionModel.setOnMessageListener(null)

        if (!connectionModel.isConnectedOrPending()) {
            connectionModel.release()
        }
    }

    fun resetConnection() {
        connectionModel.restart()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun disconnect() {
        connectionModel.sendDisconnectRequest()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun connectToDevice() {

        val device = scanModel.getDeviceByAddress(deviceAddress)

        if (device != null) {
            view.showStatusPending()
            connectionModel.connect(device)
            view.showWainingForOpponent()
        } else {
            view.showStatusNotConnected()
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
        if (scanModel.isBluetoothEnabled()) {
            connectToDevice()
            view.showStatusPending()
            view.showWainingForOpponent()
        } else {
            view.showBluetoothDisabled()
        }
    }

    fun acceptConnection() {
        view.hideActions()
        view.showStatusConnected()
        connectionModel.acceptConnection()
    }

    fun rejectConnection() {
        view.hideActions()
        view.showStatusNotConnected()
        connectionModel.rejectConnection()
        updateState()
    }

    fun onBluetoothEnabled() {
        prepareConnection()
    }

    fun onBluetoothEnablingFailed() {
        view.showBluetoothEnablingFailed()
    }

    fun enableBluetooth() {
        view.requestBluetoothEnabling()
    }

    private fun updateState() {
        val currentConversation: Conversation? = connectionModel.getCurrentConversation()
        if (currentConversation == null) {
            if (connectionModel.isPending()) {
                view.showStatusPending()
                view.showWainingForOpponent()
            } else {
                view.showStatusNotConnected()
                view.showNotConnectedToAnyDevice()
            }
        } else if (currentConversation.deviceAddress != deviceAddress) {
            view.showStatusNotConnected()
            view.showNotConnectedToThisDevice("${currentConversation.displayName} (${currentConversation.deviceName})")
        } else if (connectionModel.isPending() && currentConversation.deviceAddress == deviceAddress) {
            view.showStatusPending()
            view.showConnectionRequest(currentConversation)
        } else {
            view.showStatusConnected()
        }
    }
}
