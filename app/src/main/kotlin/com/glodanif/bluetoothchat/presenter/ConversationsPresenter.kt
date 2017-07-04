package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsPresenter(private val view: ConversationsView, private val connection: BluetoothConnector,
                             private val storage: ConversationsStorage, private val settings: SettingsManager) {

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {

            connection.setOnConnectListener(connectionListener)
            connection.setOnMessageListener(messageListener)
            view.connectedToModel()

            loadConversations()

            val device = connection.getCurrentConversation()

            if (device != null && connection.isPending()) {
                view.notifyAboutConnectedDevice(device)
            }
        }

        override fun onError() {
            connection.setOnPrepareListener(null)
            connection.setOnConnectListener(null)
            connection.setOnMessageListener(null)
        }
    }

    private val connectionListener = object : OnConnectionListener {

        override fun onConnectionAccepted() {

        }

        override fun onConnectionRejected() {

        }

        override fun onConnectedIn(conversation: Conversation) {
            view.notifyAboutConnectedDevice(conversation)
        }

        override fun onConnectedOut(conversation: Conversation) {
            view.redirectToChat(conversation)
        }

        override fun onConnecting() {

        }

        override fun onConnectionLost() {

        }

        override fun onConnectionFailed() {

        }

        override fun onDisconnected() {

        }
    }

    private val messageListener = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {

        }

        override fun onMessageSent(message: ChatMessage) {

        }

        override fun onMessageDelivered(id: String) {

        }

        override fun onMessageNotDelivered(id: String) {

        }

        override fun onMessageSeen(id: String) {

        }
    }

    fun loadConversations() {
        storage.getConversations {
            if (it.isEmpty()) view.showNoConversations() else
                view.showConversations(it, connection.getCurrentConversation()?.deviceAddress)
        }
    }

    fun onStart() {
        connection.setOnPrepareListener(prepareListener)
        connection.prepare()
        view.setupUserProfile(settings.getUserName(), settings.getUserColor())
    }

    fun onStop() {
        connection.release()
    }

    fun startChat(conversation: Conversation) {
        connection.acceptConnection()
        view.hideActions()
        view.redirectToChat(conversation)
    }

    fun rejectConnection() {
        view.hideActions()
        connection.rejectConnection()
    }

    fun sendMessage(message: String) {
        connection.sendMessage(message)
    }

    fun onConnect(device: BluetoothDevice) {
        connection.connect(device)
    }
}
