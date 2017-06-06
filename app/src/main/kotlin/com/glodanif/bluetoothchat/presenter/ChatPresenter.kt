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

            }

            override fun onError() {

            }
        })

        connectionModel.setOnConnectListener(object : OnConnectionListener {

            override fun onConnectedIn(device: BluetoothDevice) {

            }

            override fun onConnectedOut(device: BluetoothDevice) {

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

        connectionModel.setOnMessageListener(object : OnMessageListener {

            override fun onMessageReceived(message: ChatMessage) {
                view.showReceivedMessage(message)
                storage.insertMessage(message)
            }

            override fun onMessageSent(message: ChatMessage) {
                view.showSentMessage(message)
                storage.insertMessage(message)
            }
        })

        storage.setListener { view.showMessagesHistory(it) }
    }

    fun onStart() {
        if (!connectionModel.isConnected()) {
            connectionModel.prepare()
        }
        storage.getMessagesByDevice(deviceAddress)
    }

    fun onStop() {
        if (!connectionModel.isConnected()) {
            connectionModel.release()
        }
    }

    fun sendMessage(message: String) {
        connectionModel.sendMessage(message)
    }

    fun onConnect(device: BluetoothDevice) {
        connectionModel.connect(device)
    }
}
