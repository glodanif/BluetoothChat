package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothDevice

interface BluetoothConnector {

    fun prepare()
    fun release()
    fun prepareForAccept()
    fun stop()
    fun setOnConnectListener(listener: OnConnectListener)
    fun setOnPrepareListener(listener: OnPrepareListener)
    fun setOnMessageListener(listener: OnMessageListener)
    fun connect(device: BluetoothDevice)
    fun sendMessage(message: String)

    interface OnConnectListener {
        fun onConnecting()
        fun onConnected(name: String)
        fun onConnectionLost()
        fun onConnectionFailed()
        fun onDisconnected()
    }

    interface OnMessageListener {
        fun onMessageReceived(message: String)
        fun onMessageSent(message: String, id: Int)
    }

    interface OnPrepareListener {
        fun onPrepared()
        fun onError()
    }
}