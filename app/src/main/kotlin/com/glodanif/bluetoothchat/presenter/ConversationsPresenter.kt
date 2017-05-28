package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsPresenter(private val view: ConversationsView, private val connection: BluetoothConnector) {

    init {
        connection.setOnPrepareListener(object : BluetoothConnector.OnPrepareListener {

            override fun onPrepared() {
                connection.prepareForAccept()
            }

            override fun onError() {

            }
        })

        connection.setOnConnectListener(object : BluetoothConnector.OnConnectListener {

            override fun onConnecting() {

            }

            override fun onConnected(device: BluetoothDevice) {
                view.redirectToChat(device)
            }

            override fun onConnectionLost() {

            }

            override fun onConnectionFailed() {

            }

            override fun onDisconnected() {

            }
        })

        connection.setOnMessageListener(object : BluetoothConnector.OnMessageListener {

            override fun onMessageReceived(message: ChatMessage) {

            }

            override fun onMessageSent(message: ChatMessage) {

            }
        })
    }

    fun onStart() {
        connection.prepare()
    }

    fun onStop() {
        connection.stop()
    }

    fun sendMessage(message: String) {
        connection.sendMessage(message)
    }

    fun onConnect(device: BluetoothDevice) {
        connection.connect(device)
    }
}
