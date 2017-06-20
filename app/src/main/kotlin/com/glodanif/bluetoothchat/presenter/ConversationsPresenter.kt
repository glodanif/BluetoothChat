package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsPresenter(private val view: ConversationsView, private val connection: BluetoothConnector,
                             private val storage: ConversationsStorage) {

    private var isRunning = false

    init {
        connection.setOnPrepareListener(object : OnPrepareListener {

            override fun onPrepared() {
                view.connectedToModel()
                connection.setConnectedToUI(true)
                storage.getConversations {
                    if (it.isEmpty()) view.showNoConversations() else
                        view.showConversations(it, connection.getCurrentlyConnectedDevice()?.address)
                }
            }

            override fun onError() {
                connection.setConnectedToUI(false)
            }
        })

        connection.setOnConnectListener(object : OnConnectionListener {

            override fun onConnectionAccepted() {

            }

            override fun onConnectionRejected() {

            }

            override fun onConnectedIn(device: BluetoothDevice) {
                if (isRunning) {
                    view.notifyAboutConnectedDevice(device)
                }
            }

            override fun onConnectedOut(device: BluetoothDevice) {
                view.redirectToChat(device)
            }

            override fun onConnecting() {

            }

            override fun onConnectionLost() {

            }

            override fun onConnectionFailed() {

            }

            override fun onDisconnected() {

            }
        })

        connection.setOnMessageListener(object : OnMessageListener {

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
        })
    }

    fun onStart() {
        isRunning = true
        connection.prepare()
    }

    fun onStop() {
        isRunning = false
        connection.release()
        connection.setConnectedToUI(false)
    }

    fun startChat(device: BluetoothDevice) {
        connection.acceptConnection()
        view.redirectToChat(device)
    }

    fun rejectConnection() {
        connection.rejectConnection()
    }

    fun sendMessage(message: String) {
        connection.sendMessage(message)
    }

    fun onConnect(device: BluetoothDevice) {
        connection.connect(device)
    }
}
