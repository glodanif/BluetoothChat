package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.view.ChatView

class ChatPresenter(private val view: ChatView, private val connectionModel: BluetoothConnector) {

    init {
        connectionModel.setOnPrepareListener(object : BluetoothConnector.OnPrepareListener {

            override fun onPrepared() {
                connectionModel.prepareForAccept()
            }

            override fun onError() {

            }
        })

        connectionModel.setOnConnectListener(object : BluetoothConnector.OnConnectListener {

            override fun onConnecting() {

            }

            override fun onConnected(name: String) {

            }

            override fun onConnectionLost() {

            }

            override fun onConnectionFailed() {

            }

            override fun onDisconnected() {

            }
        })

        connectionModel.setOnMessageListener(object : BluetoothConnector.OnMessageListener {

            override fun onMessageReceived(message: ChatMessage) {
                view.showReceivedMessage(message)
            }

            override fun onMessageSent(message: ChatMessage) {
                view.showSentMessage(message)
            }
        })
    }

    fun onStart() {
        if (!connectionModel.isConnected()) {
            connectionModel.prepare()
        }
    }

    fun onStop() {
        connectionModel.stop()
    }

    fun sendMessage(message: String) {
        connectionModel.sendMessage(message)
    }

    fun onConnect(device: BluetoothDevice) {
        connectionModel.connect(device)
    }
}
