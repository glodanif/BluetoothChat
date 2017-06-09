package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsPresenter(private val view: ConversationsView, private val connection: BluetoothConnector,
                             private val storage: ConversationsStorage) {

    init {
        connection.setOnPrepareListener(object : OnPrepareListener {

            override fun onPrepared() {
                view.connectedToModel()
            }

            override fun onError() {

            }
        })

        connection.setOnConnectListener(object : OnConnectionListener {

            override fun onConnectedIn(device: BluetoothDevice) {
                view.notifyAboutConnectedDevice(device)
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
        })
    }

    fun onStart() {
        if (!connection.isConnected()) {
            connection.prepare()
        }
        connection.setConnectedToUI(true)
        storage.getConversations {
            if (it.isEmpty()) view.showNoConversations() else view.showConversations(it)
        }
    }

    fun onStop() {
        if (!connection.isConnected()) {
            connection.release()
        }
        connection.setConnectedToUI(false)
    }

    fun startChat(device: BluetoothDevice) {
        view.redirectToChat(device)
    }

    fun rejectConnection() {
        connection.restart()
    }

    fun sendMessage(message: String) {
        connection.sendMessage(message)
    }

    fun onConnect(device: BluetoothDevice) {
        connection.connect(device)
    }
}
