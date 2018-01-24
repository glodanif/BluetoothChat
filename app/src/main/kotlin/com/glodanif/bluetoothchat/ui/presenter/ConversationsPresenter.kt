package com.glodanif.bluetoothchat.ui.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.view.ConversationsView
import javax.inject.Inject

class ConversationsPresenter(private val view: ConversationsView, private val connection: BluetoothConnector) {

    @Inject
    lateinit var conversationStorage: ConversationsStorage
    @Inject
    lateinit var settings: SettingsManager

    init {
        ComponentsManager.getDataSourceComponent().inject(this)
    }

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {

            connection.setOnConnectListener(connectionListener)
            connection.setOnMessageListener(messageListener)

            loadConversations()

            val device = connection.getCurrentConversation()

            if (device != null && connection.isPending()) {
                view.notifyAboutConnectedDevice(device)
            } else {
                view.hideActions()
            }
        }

        override fun onError() {
            connection.setOnPrepareListener(null)
            connection.setOnConnectListener(null)
            connection.setOnMessageListener(null)
        }
    }

    private val connectionListener = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {

        }

        override fun onConnectionWithdrawn() {
            view.hideActions()
        }

        override fun onConnectionDestroyed() {
            view.showServiceDestroyed()
        }

        override fun onConnectionAccepted() {
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
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
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
            view.hideActions()
        }

        override fun onConnectionFailed() {
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
            view.hideActions()
        }

        override fun onDisconnected() {
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
            view.hideActions()
        }
    }

    private val messageListener = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            loadConversations()
        }

        override fun onMessageSent(message: ChatMessage) {
            loadConversations()
        }

        override fun onMessageDelivered(id: String) {

        }

        override fun onMessageNotDelivered(id: String) {

        }

        override fun onMessageSeen(id: String) {

        }
    }

    fun loadConversations() {
        conversationStorage.getConversations {
            if (it.isEmpty()) {
                view.showNoConversations()
            } else {
                val connectedDevice = if (connection.isConnected())
                    connection.getCurrentConversation()?.deviceAddress else null
                view.showConversations(it, connectedDevice)
            }
        }
    }

    fun prepareConnection() {
        connection.setOnPrepareListener(prepareListener)
        connection.prepare()
        view.dismissConversationNotification()
    }

    fun loadUserProfile() {
        view.showUserProfile(settings.getUserName(), settings.getUserColor())
    }

    fun releaseConnection() {
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

    fun removeConversation(conversation: Conversation) {
        connection.sendDisconnectRequest()
        conversationStorage.removeConversation(conversation)
        view.removeFromShortcuts(conversation.deviceAddress)
        loadConversations()
    }

    fun disconnect() {
        connection.sendDisconnectRequest()
        loadConversations()
    }
}
